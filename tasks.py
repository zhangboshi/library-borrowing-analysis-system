from services import run_daily_tasks


def main():
    result = run_daily_tasks()
    print(
        f"Updated {len(result['updated_records'])} records; "
        f"created {len(result['notifications'])} notifications"
    )


if __name__ == "__main__":
    main()
