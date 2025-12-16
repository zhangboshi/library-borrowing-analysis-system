# library-borrowing-analysis-system

Simple Python-based demo for managing borrowing records with renewal rules, overdue fines, and a minimal web UI.

## Setup
Python 3.11+ is recommended. No external dependencies are required beyond the standard library.

## Running the app
```bash
python app.py
```
The server serves the API under `/api` and the demo UI at `http://localhost:5000/`.

## Daily tasks
Run the daily overdue calculation and reminder job manually:
```bash
python tasks.py
```
A background scheduler also runs this job once per day while the server is running.

## Tests
```bash
python -m unittest
```
