# scheduled-daily-programs

Every day at **6:00 PM IST** a GitHub Actions workflow commits **2 new Java DSA / algorithm programs**
to this repo. Nothing runs on your machine.

```
.github/workflows/daily-java-commit.yml   the schedule (cron 30 12 * * * UTC = 18:00 IST)
tools/DailyPublisher.java                 picks the next 2 programs, writes them to solutions/yyyy/MM/dd/
bank/                                     ready-made programs, used in filename order (12 = 6 days)
state/next-index.txt                      how many programs have been published so far
solutions/                                the daily output
LOG.md                                    one row per day
```

## How it works
1. The workflow starts on schedule (or manually via **Actions -> Run workflow**).
2. `DailyPublisher` takes the next 2 files from `bank/`. When the bank runs out it asks Claude for new ones
   (needs the `ANTHROPIC_API_KEY` secret). Without the key it fails with a clear message.
3. Each new program is compiled and run with `-ea`; a failing program blocks the commit.
4. The workflow commits and pushes to `main`.

## Configuration (Settings -> Secrets and variables -> Actions)
| Name | Type | Purpose |
|---|---|---|
| `GIT_USER_NAME` | variable | Author name on commits (default: repo owner) |
| `GIT_USER_EMAIL` | variable | Author email. Use your GitHub email so commits show on your contribution graph |
| `GH_PAT` | secret, optional | Personal access token (contents: write) to push as yourself. Defaults to the built-in `GITHUB_TOKEN` |
| `ANTHROPIC_API_KEY` | secret, optional | Lets the job generate new programs after `bank/` is used up |
| `ANTHROPIC_MODEL` | variable, optional | Model for generation (default `claude-sonnet-5-5`) |

Also enable **Settings -> Actions -> General -> Workflow permissions -> Read and write permissions**.

Use a token, not your GitHub password: GitHub no longer accepts passwords for git operations.

## Adding your own programs
Drop a `NN_ClassName.java` file in `bank/` (public class, a `main` that throws on failure). They are used in
filename order.

## Notes
- GitHub cron runs on UTC and can start a few minutes late under load.
- GitHub disables scheduled workflows after 60 days with no repo activity; the daily commits count as activity.
