🧠 AI Code Reviewer Application

An AI-powered Code Reviewer web application that analyzes source code and provides intelligent suggestions, recommendations, and best practices using the OpenAI API.
Users can drag & drop files or paste code, receive instant feedback, and view their review history later.

🚀 Features

🔍 AI-Based Code Review

Detects code issues, improvements, and best practices

Provides clean code suggestions and explanations

📂 Multiple Input Methods

Drag & Drop code files

Copy & Paste code directly into the editor

🕒 Persistent Review History

Stores previous reviews in MySQL

Users can revisit and compare past code reviews

🌐 Modern Web Interface

Responsive UI using HTML, CSS, and JavaScript

Clean editor-like experience

🔐 Secure Backend

Spring Boot REST APIs

Environment-based OpenAI API key management

🏗️ Tech Stack
Frontend

HTML5

CSS3

JavaScript (Vanilla JS)

Drag & Drop API

Backend

Java

Spring Boot

Spring Web

Spring Data JPA

REST APIs

Database

MySQL

AI Integration

OpenAI API (for code analysis and suggestions)

📐 System Architecture
Frontend (HTML/CSS/JS)
        |
        | REST API
        v
Spring Boot Backend
        |
        | JPA / Hibernate
        v
      MySQL
        |
        v
   OpenAI API

🖥️ Application Flow

User uploads a code file or pastes code into the editor

Frontend sends code to backend API

Backend calls OpenAI API for analysis

AI response is processed and stored in MySQL

Suggestions are returned to the UI

User can view review history anytime

📂 Project Structure
Backend (Spring Boot)
ai-code-reviewer-backend
 ┣ 📂 controller
 ┃ ┗ CodeReviewController.java
 ┣ 📂 service
 ┃ ┣ CodeReviewService.java
 ┃ ┗ OpenAiService.java
 ┣ 📂 repository
 ┃ ┗ ReviewHistoryRepository.java
 ┣ 📂 entity
 ┃ ┗ ReviewHistory.java
 ┣ 📂 dto
 ┃ ┗ CodeReviewRequest.java
 ┣ 📂 config
 ┃ ┗ OpenAiConfig.java
 ┗ AiCodeReviewerApplication.java

Frontend
ai-code-reviewer-frontend
 ┣ 📄 index.html
 ┣ 📄 style.css
 ┗ 📄 script.js

🗄️ Database Schema
review_history Table
Column Name	Type	Description
id	BIGINT	Primary Key
code	TEXT	User-submitted code
suggestion	TEXT	AI-generated suggestions
language	VARCHAR	Programming language
created_at	TIMESTAMP	Review time
🔑 Environment Variables

Create an application.properties file:

spring.datasource.url=jdbc:mysql://localhost:3306/ai_code_reviewer
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update

openai.api.key=YOUR_OPENAI_API_KEY


⚠️ Never commit your OpenAI API key to GitHub

📡 REST API Endpoints
Submit Code for Review
POST /api/review


Request Body

{
  "code": "public class HelloWorld { }",
  "language": "Java"
}


Response

{
  "suggestions": "Use meaningful class names and add comments."
}

Get Review History
GET /api/review/history

🎨 UI Features

Drag & Drop upload zone

Code text editor

Syntax-friendly formatting

Review output panel

History list with timestamps

🧪 Future Enhancements

🔐 User authentication (JWT)

🌈 Syntax highlighting (Monaco Editor)

📊 Code quality score

🧠 Multi-model AI support

🗂️ Filter history by language or date

📥 Download review reports (PDF)

📌 Use Cases

Developers reviewing their code

Freshers preparing for interviews

Learning clean code & best practices

Peer code review assistance

🧑‍💻 Author

Md Anwar Alam
B.Tech Computer Science
Java | Spring Boot | MySQL | AI Integration
