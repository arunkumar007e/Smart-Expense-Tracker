@echo off
REM ============================================================
REM  Personal Expense Tracker - Windows convenience launcher
REM
REM  Starts the backend (Spring Boot on :8080) and the
REM  frontend (Vite dev server on :5173).
REM
REM  Database: uses DB_URL / DB_USERNAME / DB_PASSWORD when set,
REM  otherwise falls back to local defaults
REM  (localhost:5432, user=postgres, password=postgres).
REM  If the database is missing it is created automatically.
REM ============================================================
setlocal

if not "%DB_USERNAME%"=="" (
    echo Using DB credentials from environment  ^(user: %DB_USERNAME%^)
) else (
    echo Using default local DB credentials  ^(user: postgres / postgres^)
    echo TIP: set DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET env vars to override.
)

echo.
echo [1/4] Checking prerequisites...
where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java not found. Install JDK 21 and add it to PATH.
    exit /b 1
)
where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven not found. Install Maven 3.9+ and add it to PATH.
    exit /b 1
)
where npm >nul 2>nul
if errorlevel 1 (
    echo ERROR: Node.js/npm not found. Install Node 18+ and add it to PATH.
    exit /b 1
)
echo Prerequisites OK.
echo.

echo [2/4] Ensuring the database exists...
set "PSQL="
where psql >nul 2>nul && set "PSQL=psql"
if "%PSQL%"=="" (
    if exist "C:\Program Files\PostgreSQL\17\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\17\bin\psql.exe"
    if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\16\bin\psql.exe"
    if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\15\bin\psql.exe"
)
if "%PSQL%"=="" (
    echo WARNING: psql not found - skipping DB setup. The backend will create
    echo          tables automatically via Hibernate, so this is optional.
) else (
    if "%DB_PASSWORD%"=="" (
        set "PGPASSWORD=postgres"
    ) else (
        set "PGPASSWORD=%DB_PASSWORD%"
    )
    "%PSQL%" -U postgres -h localhost -p 5432 -lqt >nul 2>nul
    set PGPASSWORD=
    if errorlevel 1 (
        echo NOTE: could not connect to PostgreSQL with defaults. If the
        echo       database already exists, the backend will still work.
    ) else (
        "%PSQL%" -U postgres -h localhost -p 5432 -lqt 2>nul | findstr /C:"expense_tracker" >nul 2>nul
        if errorlevel 1 (
            echo Creating database and schema...
            set PGPASSWORD=postgres
            "%PSQL%" -U postgres -h localhost -p 5432 -f "%~dp0database\create_database.sql"
            set PGPASSWORD=
        ) else (
            echo Database "expense_tracker" already exists.
        )
    )
)
echo.

echo [3/4] Starting backend (Spring Boot on :8080)...
start "Expense-Tracker-Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"
echo Backend starting... waiting for it to come up.
timeout /t 25 /nobreak >nul
echo.

echo [4/4] Installing frontend dependencies and starting Vite dev server (:5173)...
cd /d %~dp0frontend
if not exist node_modules (
    call npm install
)
start "Expense-Tracker-Frontend" cmd /k "npm run dev"

echo.
echo ============================================
echo  App:       http://localhost:5173
echo  Swagger:   http://localhost:8080/swagger-ui.html
echo ============================================
echo.
echo Close the "Expense-Tracker-Frontend" and
echo "Expense-Tracker-Backend" windows to stop.
endlocal
pause