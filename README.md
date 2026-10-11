# scheduled-daily-programs

Every day at **6:00 PM IST** a GitHub Actions workflow commits **2 new Java DSA / algorithm programs**
to this repo. It runs entirely on GitHub, costs nothing, and needs no secrets or tokens.

```
.github/workflows/daily-java-commit.yml   the schedule (cron 30 12 * * * UTC = 18:00 IST)
tools/DailyPublisher.java                 picks the next 2 programs, writes them to solutions/yyyy/MM/dd/
bank/                                     20 ready-made programs, used in filename order (10 days)
state/next-index.txt                      how many programs have been published so far
solutions/                                the daily output
LOG.md                                    one row per day
```

## How it works
1. The workflow starts on schedule (or manually via **Actions -> Run workflow**).
2. `DailyPublisher` copies the next 2 files from `bank/` into today's folder. After the last program it
   starts over from the first one.
3. Each new program is compiled and run with `-ea`; a failing program blocks the commit.
4. The workflow commits and pushes to `main` using the `GITHUB_TOKEN` GitHub provides automatically.

## One-time setup
1. Settings -> Actions -> General -> Workflow permissions -> **Read and write permissions**.
2. Optional (so commits count on your contribution graph): Settings -> Secrets and variables -> Actions ->
   **Variables** -> add `GIT_USER_NAME` and `GIT_USER_EMAIL` (your GitHub email). These are plain variables, not secrets.

## Adding more programs
Drop a `NN_ClassName.java` file in `bank/` (public class, a `main` that throws on failure). Files are used in
filename order.

## Notes
- GitHub cron runs on UTC and can start a few minutes late under load.
- GitHub disables scheduled workflows after 60 days with no repo activity; the daily commits count as activity.
