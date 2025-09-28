LifePilot v2.0.0
================

LifePilot is an application designed to track and improve your key life metrics. 
In this new version, the frontend is built with FlutterFlow, and the Telegram bot has been completely removed.

Main Features:
--------------
- Life tracking:
    * Physical state 💪
    * Mental state 🧠
    * Social environment 🌐
    * Goals and actions 🎯
- Voice recognition via Whisper API 🎙️.
- Conversational AI using Ollama.
- Improved prompts and redesigned architecture.
- Email-based authentication with mandatory email verification.

Technical Details:
------------------
- Backend: Java + Spring Framework
- Database: PostgreSQL
- Whisper API: FastAPI (Python), running in a separate container
- Ollama: for conversational AI
- All services run in Docker containers

Getting Started:
----------------
1. Clone the repository.

2. Copy example environment files:
   cp ./backend/env/.env.server.example ./backend/env/.env.server  
   cp ./backend/env/.env.postgres.example ./backend/env/.env.postgres

3. Edit the copied .env files to add required secrets:
   - .env.server — add API keys and server-related secrets
   - .env.postgres — add PostgreSQL username, password, and connection details

Future Plans:
-------------
- Mobile app enhancements in FlutterFlow
- Integration with smart devices and fitness trackers
- Reminders and progress tracking for life metrics
- Improved AI personalization using user data

Contributing:
-------------
LifePilot is fully open-source. Contributions and feedback are welcome!

Contact:
--------
For questions or suggestions, open an issue or contact directly.

Thank you for using LifePilot — your companion on the journey to better living. 🌟
