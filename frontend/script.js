
const AUTH_TOKEN_KEY = 'ai_code_reviewer_access_token';
const MAX_CODE_LENGTH = 50000;
let historyCurrentPage = 0;
let historyTotalPages = 0;

function getApiBase() {
    const configuredUrl = window.AI_CODE_REVIEWER_API_URL?.trim();
    if(!configuredUrl) {
        throw new Error('Backend URL is not configured. Set it in api-config.js.');
    }

    const url = new URL(configuredUrl);
    if(!['http:', 'https:'].includes(url.protocol)) {
        throw new Error('Backend URL must use HTTP or HTTPS.');
    }
    if(url.protocol === 'http:' && !['localhost', '127.0.0.1'].includes(url.hostname)) {
        throw new Error('The production backend URL must use HTTPS.');
    }
    if(window.location.protocol === 'https:' && url.protocol !== 'https:') {
        throw new Error('The backend URL must use HTTPS on a secure page.');
    }
    return url.toString().replace(/\/+$/, '');
}

async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});
    const token = sessionStorage.getItem(AUTH_TOKEN_KEY);
    if(token) {
        headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(`${getApiBase()}${path}`, {
        ...options,
        headers
    });

    if(response.status === 401 && !path.includes('/auth/login') && !path.includes('/auth/register')) {
        sessionStorage.removeItem(AUTH_TOKEN_KEY);
        window.location.replace('index.html');
    }
    return response;
}

async function readApiData(response) {
    const payload = await response.json();
    return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;
}

async function readApiError(response, fallbackMessage) {
    try {
        const payload = await response.json();
        return payload?.message || fallbackMessage;
    } catch(e) {
        return fallbackMessage;
    }
}

function isValidId(id) {
    return id && id !== 'undefined' && id !== 'null';
}

function isStoredAnalysisError(analysis) {
    return typeof analysis === 'string' && analysis.trim().startsWith('Error:');
}

function getAuthToken() {
    return sessionStorage.getItem(AUTH_TOKEN_KEY);
}

function showAuthMessage(message, isError = false) {
    const element = document.getElementById('authMessage');
    if(element) {
        element.innerText = message;
        element.style.color = isError ? 'var(--danger)' : 'var(--success)';
    }
}

function setAuthMode(mode) {
    const registerMode = mode === 'register';
    const submitButton = document.getElementById('authSubmitButton');
    const modeButton = document.getElementById('authModeButton');
    const passwordInput = document.getElementById('authPassword');
    const heading = document.getElementById('authHeading');
    if(!submitButton || !modeButton || !passwordInput || !heading) return;

    submitButton.innerText = registerMode ? 'Create account' : 'Sign in';
    modeButton.innerText = registerMode ? 'I already have an account' : 'Create an account';
    passwordInput.minLength = registerMode ? 12 : 1;
    passwordInput.autocomplete = registerMode ? 'new-password' : 'current-password';
    heading.innerText = registerMode ? 'Create your account' : 'Sign in to your account';
    document.getElementById('authForm').dataset.mode = mode;
    showAuthMessage('');
}

function toggleAuthMode() {
    const form = document.getElementById('authForm');
    setAuthMode(form.dataset.mode === 'register' ? 'login' : 'register');
}

async function initializeAuthentication() {
    const authPanel = document.getElementById('authPanel');
    const reviewPanel = document.getElementById('reviewPanel');
    const signOutButton = document.getElementById('signOutButton');
    if(!authPanel || !reviewPanel) return;

    const signedIn = Boolean(getAuthToken());
    authPanel.hidden = signedIn;
    reviewPanel.hidden = !signedIn;
    if(signOutButton) signOutButton.hidden = !signedIn;

    if(signedIn) return;

    setAuthMode('login');
    try {
        getApiBase();
    } catch(error) {
        showAuthMessage(error.message, true);
    }
    document.getElementById('authForm').addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const mode = form.dataset.mode;
        const submitButton = document.getElementById('authSubmitButton');
        submitButton.disabled = true;
        showAuthMessage('Connecting…');

        try {
            const response = await apiFetch(`/api/v1/auth/${mode}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    email: document.getElementById('authEmail').value,
                    password: document.getElementById('authPassword').value
                })
            });
            if(!response.ok) {
                throw new Error(await readApiError(response, 'Authentication failed'));
            }
            const authResponse = await readApiData(response);
            if(!authResponse?.accessToken) {
                throw new Error('The server returned an invalid authentication response.');
            }
            sessionStorage.setItem(AUTH_TOKEN_KEY, authResponse.accessToken);
            window.location.reload();
        } catch(error) {
            showAuthMessage(error.message || 'Unable to authenticate.', true);
        } finally {
            submitButton.disabled = false;
        }
    });
}

function signOut() {
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    window.location.replace('index.html');
}

async function submitCode() {
    
    const fileInput = document.getElementById('fileInput');
    const codeArea = document.getElementById('codeArea');
    const analyzeBtn = document.getElementById('analyzeBtn');

    let code  = codeArea.value;
    let filename = 'pasted_code.txt';

    if(fileInput && fileInput.files.length > 0) {
        const file = fileInput.files[0];
        if(file.size > MAX_CODE_LENGTH * 4) {
            alert(`Files must be ${MAX_CODE_LENGTH.toLocaleString()} characters or fewer.`);
            return;
        }
        code = await file.text();
        filename = file.name;
    }
    
    if(!code.trim()) {
        alert('Please enter code or upload a file');
        return;
    }
    if(code.length > MAX_CODE_LENGTH) {
        alert(`Code must be ${MAX_CODE_LENGTH.toLocaleString()} characters or fewer.`);
        return;
    }

    analyzeBtn.disabled = true;
    analyzeBtn.innerText = 'Uploading...';


    // call backend API

    try {
        const response = await apiFetch('/api/v1/code/upload', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json'},
            body: JSON.stringify({
                filename: filename,
                language: detectLanguage(filename),
                code: code
            })
        });

        if(response.ok) {
            const submission = await readApiData(response);

            if(!submission || !isValidId(submission.id)) {
                throw new Error('Upload response did not include a submission id');
            }

            // trigger analysis api
            const analysisResponse = await apiFetch(`/api/v1/code/analyze/${submission.id}`, {method: 'POST'});
            if(!analysisResponse.ok) {
                throw new Error(await readApiError(analysisResponse, 'Analysis failed'));
            }
            window.location.href = `review.html?id=${submission.id}`;

            // do something
        } else {
            throw new Error(await readApiError(response, 'Upload failed'));
        }
    } catch(e) {
        console.error(e);
        alert(e.message || 'Error connecting to server');
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
    if(!getAuthToken()) {
        window.location.replace('index.html');
        return;
    }
    const params = new URLSearchParams(window.location.search)
    const submissionId = params.get('id')

    if(!isValidId(submissionId)) return ;
    
    
    const analysisDiv = document.getElementById('aiAnalysis');
    const loadingDiv = document.getElementById('loadingAnalysis');
    const codeView = document.getElementById('codeView');
    const versionBadge = document.getElementById('versionBadge');
    

    const res = await apiFetch(`/api/v1/code/version/${submissionId}`);

    if(res.ok) {
        const versions = await readApiData(res);
        if(!Array.isArray(versions)) return;

        const latest = versions.sort((a, b) => b.versionNumber - a.versionNumber)[0];

        if(latest) {
            codeView.value = latest.code;
            versionBadge.innerText = `Version ${latest.versionNumber}`;

            if(latest.analysis === 'Pending analysis...' || !latest.analysis) {
                loadingDiv.style.display = 'block';

                setTimeout(() => loadReviewPage(), 2000);
            }
            else if(isStoredAnalysisError(latest.analysis)) {
                loadingDiv.style.display = 'block';
                analysisDiv.style.display = 'none';

                const analysisResponse = await apiFetch(`/api/v1/code/analyze/${submissionId}`, {method: 'POST'});
                if(analysisResponse.ok) {
                    setTimeout(() => loadReviewPage(), 500);
                }
                else {
                    loadingDiv.style.display = 'none';
                    analysisDiv.style.display = 'block';
                    analysisDiv.innerText = await readApiError(analysisResponse, latest.analysis);
                }
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
    if(!getAuthToken()) {
        window.location.replace('index.html');
        return;
    }
    const params = new URLSearchParams(window.location.search);
    const submissionId = params.get('id');
    const code = document.getElementById('newVersionCode').value;
    const btn = document.getElementById('submitVersionBtn')

    if(!isValidId(submissionId)) return alert('Missing submission id');
    if(!code.trim()) return alert('Please enter code');

    if(btn) {
        btn.disabled = true;
        btn.innerText = 'Analyzing...';
    }

    try {
        const res = await apiFetch(`/api/v1/code/version/${submissionId}/new`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({code})
        });

        if(res.ok) {
            const analysisResponse = await apiFetch(`/api/v1/code/analyze/${submissionId}`, {method: 'POST'});
            if(!analysisResponse.ok) {
                throw new Error(await readApiError(analysisResponse, 'Analysis failed'));
            }

            window.location.reload();
        }
        else throw new Error(await readApiError(res, 'Failed to submit version'));
    }
    catch(e) {
        console.error(e);
        alert(e.message || 'Error submitting version');
        if(btn) {
            btn.disabled = false;
            btn.innerText = 'Submit New Version';
        }
    }
}

async function loadHistory() {
    if(!getAuthToken()) {
        window.location.replace('index.html');
        return;
    }
    const list = document.getElementById('submissionList');
    if(!list) return ;

    try {
        const res = await apiFetch(`/api/v1/code/submissions/me?page=${historyCurrentPage}&size=20`);

        if(res.ok) {
            const payload = await res.json();
            const submissions = payload.data;
            historyTotalPages = payload.meta?.totalPages || 0;
            list.replaceChildren();

            if(!Array.isArray(submissions)) {
                list.innerHTML = '<div class="card" style="text-align:center">Unable to load submissions.</div>';
                return ;
            }

            if(submissions.length === 0) {
                list.innerHTML = '<div class="card" style="text-align:center"> No submissions yet. </div>';
                document.getElementById('historyPaging').hidden = true;
                return ;
            }

            submissions.forEach(sub => {
                const item = document.createElement('div');
                item.className = 'card';
                const row = document.createElement('div');
                row.style.display = 'flex';
                row.style.justifyContent = 'space-between';
                row.style.alignItems = 'center';
                const title = document.createElement('h3');
                title.innerText = sub.filename;
                const actions = document.createElement('div');
                const viewLink = document.createElement('a');
                viewLink.href = `review.html?id=${encodeURIComponent(sub.id)}`;
                viewLink.className = 'btn btn-primary';
                viewLink.innerText = 'View';
                const compareLink = document.createElement('a');
                compareLink.href = `compare.html?id=${encodeURIComponent(sub.id)}`;
                compareLink.className = 'btn btn-secondary';
                compareLink.innerText = 'Compare';
                actions.append(viewLink, compareLink);
                row.append(title, actions);
                item.append(row);
                list.appendChild(item);
            });
            const paging = document.getElementById('historyPaging');
            paging.hidden = historyTotalPages <= 1;
            document.getElementById('historyPageLabel').innerText =
                `Page ${historyCurrentPage + 1} of ${historyTotalPages}`;
            document.getElementById('previousPageButton').disabled = historyCurrentPage === 0;
            document.getElementById('nextPageButton').disabled = historyCurrentPage + 1 >= historyTotalPages;
        }
    }
    catch(e) {
        list.innerText = "Error loading History";
    }   
}

function changeHistoryPage(offset) {
    const newPage = historyCurrentPage + offset;
    if(newPage < 0 || newPage >= historyTotalPages) return;
    historyCurrentPage = newPage;
    loadHistory();
}


async function loadComparisonPage() {
    if(!getAuthToken()) {
        window.location.replace('index.html');
        return;
    }
    const params = new URLSearchParams(window.location.search);
    const submissionId = params.get('id');

    if(!isValidId(submissionId)) return;

    const res = await apiFetch(`/api/v1/code/version/${submissionId}`);
    if(res.ok) {
        const versions = await readApiData(res);
        if(!Array.isArray(versions)) return;

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
    if(!getAuthToken()) {
        window.location.replace('index.html');
        return;
    }
    const vAId = document.getElementById('versionA').value;
    const vBId = document.getElementById('versionB').value;

    if(vAId && vBId && vAId != 'Select version A' && vBId != 'Select version B') {
        const res = await apiFetch('/api/v1/code/compare', {
            method: 'POST',
            headers: {'Content-Type' : 'application/json'},
            body: JSON.stringify({versionA : vAId, versionB: vBId})
        });

        if(res.ok) {
            const data = await readApiData(res);
            document.getElementById('codeA').innerText = data.versionA.code;
            document.getElementById('codeB').innerText = data.versionB.code;
        } 
    }
}
