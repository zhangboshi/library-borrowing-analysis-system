import json
import re
import threading
import time
from http.server import HTTPServer, SimpleHTTPRequestHandler
from pathlib import Path

from services import list_records, renew_record, run_daily_tasks

STATIC_DIR = Path(__file__).parent / "static"


class LibraryHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(STATIC_DIR), **kwargs)

    def do_GET(self):
        if self.path.startswith("/api/records"):
            self._send_json(list_records())
        elif self.path.startswith("/api/health"):
            self._send_json({"status": "ok"})
        else:
            super().do_GET()

    def do_POST(self):
        match = re.match(r"^/api/records/(\d+)/renew$", self.path)
        if match:
            record_id = int(match.group(1))
            try:
                data = renew_record(record_id)
                self._send_json(data, status=200)
            except ValueError as exc:
                self._send_json({"error": str(exc)}, status=400)
            return
        self.send_error(404, "Not Found")

    def log_message(self, format, *args):
        # Reduce noise in the console output
        return

    def _send_json(self, data, status: int = 200):
        body = json.dumps(data).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def start_scheduler():
    def worker():
        while True:
            run_daily_tasks()
            time.sleep(24 * 60 * 60)

    thread = threading.Thread(target=worker, daemon=True)
    thread.start()


def main():
    start_scheduler()
    server = HTTPServer(("0.0.0.0", 5000), LibraryHandler)
    print("Server running at http://0.0.0.0:5000")
    server.serve_forever()


if __name__ == "__main__":
    main()
