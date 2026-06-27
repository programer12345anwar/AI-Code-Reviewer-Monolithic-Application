
const API_BASE = 'http://localhost:9095/api/code';
const USER_ID_KEY = 'ai_codereviewer_user_id';

function getUserId() {
    let id = localStorage.getItem(USER_ID_KEY);
    if(!id) {
        id = crypto.randomUUID();
        localStorage.setItem(USER_ID_KEY, id);
    }
    return id;
}

const USER_ID = getUserId();

async function submitCode() {
    
    const fileInput = document.getElementById('fileInput');
    const codeArea = document.getElementById('codeArea');
    const analyzeBtn = document.getElementById('analyzeBtn');

    let code  = codeArea.value;
    let filename = 'pasted_code.txt';

    if(fileInput && fileInput.files.length > 0) {
        const file = fileInput.files[0];
        code = await file.text();
        filename = file.name;
    }
    
    if(!code.trim()) {
        alert('Please enter code or upload a file');
        return;
    }

    analyzeBtn.disabled = true;
    analyzeBtn.innerText = 'Uploading...';


    // call backend API

    try {
        const reponse = await fetch(`${API_BASE}/upload`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json'},
            body: JSON.stringify({
                userId: USER_ID,
                filename: filename,
                language: detectLanguage(filename),
                code: code
            })
        });

        if(reponse.ok) {
            const submission = await reponse.json();

            // trigger analysis api
            await fetch(`${API_BASE}/analyze/${submission.id}`, {method: 'POST'});
            window.location.href = `review.html?id=${submission.id}`;

            // do something
        } else {
            alert('Upload failed');
        }
    } catch(e) {
        console.error(e);
        alert('Error connection to server');
    } finally {
        analyzeBtn.disabled = false;
        analyzeBtn.innerText = 'Analyze Code';
    }
    
}


function detectLanguage(filename) {
    if(filename.endsWith('.js')) return 'javascript';
    if(filename.endsWith('.java')) return 'java';
    if(filename.endsWith('.py')) return 'python';
    if(filename.endsWith('.cpp')) return 'cpp';

    return 'text';

}

async function loadReviewPage() {
    const params = new URLSearchParams(window.location.search)
    const submissionId = params.get('id')

    if(!submissionId) return ;
    
    const analysisDiv = document.getElementById('aiAnalysis');
    const loadingDiv = document.getElementById('loadingAnalysis');
    const codeView = document.getElementById('codeView');
    const versionBadge = document.getElementById('versionBadge');
    

    const res = await fetch(`${API_BASE}/version/${submissionId}`);

    if(res.ok) {
        const versions = await res.json();
        const latest = versions.sort((a, b) => b.versionNumber - a.versionNumber)[0];

        if(latest) {
            codeView.value = latest.code;
            versionBadge.innerText = `Version ${latest.versionNumber}`;

            if(latest.analysis === 'Pending analysis...' || !latest.analysis) {
                loadingDiv.style.display = 'block';

                setTimeout(() => loadReviewPage(), 2000);
            }
            else {
                loadingDiv.style.display = 'none';
                analysisDiv.style.display = 'block';
                analysisDiv.innerText = latest.analysis;
            }
        }
    }

}

async function submitNewVersion() {
    const params = new URLSearchParams(window.location.search);
    const submissionId = params.get('id');
    const code = document.getElementById('newVersionCode').value;
    const btn = document.getElementById('submitVersionBtn')

    if(!code.trim()) return alert('Please enter code');

    if(btn) {
        btn.disabled = true;
        btn.innerText = 'Analyzing...';
    }

    try {
        const res = await fetch(`${API_BASE}/version/${submissionId}/new`, {
            method: 'POST',
            body: code
        });

        if(res.ok) {
            await fetch(`${API_BASE}/analyze/${submissionId}`, {method: 'POST'});

            window.location.reload();
        }
        else {
            alert('Failed to submit verison');
            if(btn) {
                btn.disabled = false;
                btn.innerText = 'Submit New Version';
            }
        }
    }
    catch(e) {
        console.error(e);
        alert('Error submitting verison');
        if(btn) {
            btn.disabled = false;
            btn.innerText = 'Submit New Version';
        }
    }
}

async function loadHistory() {
    const list = document.getElementById('submissionList');
    if(!list) return ;

    try {
        const res = await fetch(`${API_BASE}/submissions/${USER_ID}`);

        if(res.ok) {
            const submissions = await res.json();
            list.innerHTML = '';

            if(submissions.length === 0) {
                list.innerHTML = '<div class="card" style="text-align:center"> No submissions yet. </div>';
                return ;
            }

            submissions.forEach(sub => {
                const item = document.createElement('div');
                item.className = 'card';
                item.innerHTML = `
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h3>${sub.filename}</h3>
                        </div>

                        <div>
                            <a href="review.html?id=${sub.id}" class="btn btn-primary"> View </a>
                            <a href="compare.html?id=${sub.id}" class="btn btn-secondary"> Compare </a>
                        <div>
                    <div>
                `;
                list.appendChild(item);
            });
        }
    }
    catch(e) {
        list.innerText = "Error loading History";
    }   
}


async function loadComparisonPage() {
    const params = new URLSearchParams(window.location.search);
    const submissionId = params.get('id');

    if(!submissionId) return;

    const res = await fetch(`${API_BASE}/version/${submissionId}`);
    if(res.ok) {
        const versions = await res.json();
        const vA = document.getElementById('versionA');
        const vB = document.getElementById('versionB');

        versions.forEach(v => {
            const opt = document.createElement('option');
            opt.value = v.id;
            opt.innerText = `Version ${v.versionNumber}`;
            vA.appendChild(opt.cloneNode(true));
            vB.appendChild(opt);
        });
    }
}


async function updateComparison() {
    const vAId = document.getElementById('versionA').value;
    const vBId = document.getElementById('versionB').value;

    if(vAId && vBId && vAId != 'Select version A' && vBId != 'Select version B') {
        const res = await fetch(`${API_BASE}/compare`, {
            method: 'POST',
            headers: {'Content-Type' : 'application/json'},
            body: JSON.stringify({versionA : vAId, versionB: vBId})
        });

        if(res.ok) {
            const data = await res.json();
            document.getElementById('codeA').innerText = data.versionA.code;
            document.getElementById('codeB').innerText = data.versionB.code;
        } 
    }
}