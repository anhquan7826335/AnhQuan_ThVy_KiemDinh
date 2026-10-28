# Music library JMeter load test

This plan targets the existing Spring Boot application, not the sample admissions URLs. Each virtual user signs in once, keeps its own session cookie, then repeatedly reads the home page, genres, genre search, artists, songs, and song search. Requests that change data are intentionally excluded.

## Prerequisites

- Start the app at `http://localhost:8080` and make sure its configured database is available.
- Create a dedicated test account with the public registration page. Do not use an administrator account.
- Install Apache JMeter 5.6.3 or newer.

## Run a short smoke test

From this directory, open the `.jmx` file in JMeter GUI, or run:

```powershell
jmeter -n -t .\music-library-load-test.jmx -Jthreads=1 -Jrampup=1 -Jduration=15 -Jusername=YOUR_TEST_USER -Jpassword=YOUR_TEST_PASSWORD -l .\smoke-results.jtl
```

## Simulated load profiles

Run each profile separately, changing the result-file name for each run:

| Profile | Threads | Ramp-up | Duration |
| --- | ---: | ---: | ---: |
| Low | 20 | 10 seconds | 180 seconds |
| Medium | 50 | 20 seconds | 180 seconds |
| High | 100 | 30 seconds | 180 seconds |

```powershell
jmeter -n -t .\music-library-load-test.jmx -Jthreads=20 -Jrampup=10 -Jduration=180 -Jusername=YOUR_TEST_USER -Jpassword=YOUR_TEST_PASSWORD -l .\low-results.jtl
jmeter -n -t .\music-library-load-test.jmx -Jthreads=50 -Jrampup=20 -Jduration=180 -Jusername=YOUR_TEST_USER -Jpassword=YOUR_TEST_PASSWORD -l .\medium-results.jtl
jmeter -n -t .\music-library-load-test.jmx -Jthreads=100 -Jrampup=30 -Jduration=180 -Jusername=YOUR_TEST_USER -Jpassword=YOUR_TEST_PASSWORD -l .\high-results.jtl
```

To test another server, set `-Jhost`, `-Jport`, or `-Jprotocol`; search terms can be changed with `-JgenreKeyword` and `-JsongKeyword`. The plan does not contain credentials. Use a disposable test account and avoid putting real credentials in shared command history or result files.

The plan inserts a random 1-3 second think time between requests, checks each page for HTTP 200, and fails the login sample if the app renders its invalid-credentials message. Inspect the JTL output or generate an HTML dashboard to calculate actual average, percentile, throughput, and error rates. The figures in the sample report are illustrative and are not results from this test plan.
