# Drunkr

Backend of the drunkr app. This ran on Heroku with mongodb.
It would start collecting checkins from Untappd or local entries,
to determine your BAC.

No longer running, but publishing the source core as publically
viewable project.

## Setup

### Required Software
* [HomeBrew](#HomeBrew) ^
* [Chrome](#Chrome)
* [ChromeDriver](#ChromeDriver)
* [Mongo](#Mongo)
* [Docker](#Docker)

^ for mac users

### Master Password
```bash
export DRUNKR_MASTER_PASSWORD=...
```

### HomeBrew
```bash
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```
[HomeBrew](https://brew.sh/)

### Chrome
[Installation Guide](https://support.google.com/chrome/answer/95346?co=GENIE.Platform%3DDesktop&hl=en)

### ChromeDriver
```bash
brew install chromedriver
```
[Resource Guide](https://chromedriver.storage.googleapis.com/index.html?path=2.35/)

### Mongo

### Docker
