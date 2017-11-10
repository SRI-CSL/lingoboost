 <!--- Banner Hotlink Test ---> 
![lingoBoost](http://cortical.csl.sri.com/images/lingoBoost_banner_v2.png)

## Websites
### mTurk User Site 
https://lingoboost.csl.sri.com/lingoturk/turk/

This site has instructions for the LingoTurk experiment we are running. This is also where users can check their current participation [status](https://lingoboost.csl.sri.com/lingoturk/turk/status). Status is a bit messy right now as we work on cleaning the system logs (e.g., we need to remove incomplete logs).

### Admin Site
https://lingoboost.csl.sri.com/lingoturk/admin/

This site requires a login and is where the researchers login to setup their experiments and user parameters.

## App Distribution
We have two distribution models.

### Google Play Beta Testing (Official Android Distribution)
*Preferred Method*

**App Updating:** The app will update automatically through the Google Play Store.

**Email Requirements:** This requires a gmail account the user has setup on the Play Store (the account they use when they download apps). This is akin to your Apple ID if we were using iPhone.

### Direct Download (Unofficial Distribution; Sideload).
**App Updating:** The app will need to be updated manually when new versions are released and emailed to the user.

**Email Requirements:** No email required. This only requires a link (https://lingoboost.csl.sri.com/lingoboost-beta.apk), but then the user has to install the app outside of the official Google/Android market. 

## App User Interface
### Starting Up
When users first start the lingoBoost app, they should enter the username that is assigned by the researcher.

![startup screen](https://user-images.githubusercontent.com/6577692/32380349-1c5d7450-c06d-11e7-9748-d1a0bad3613f.png)

If users need to adjust their username, they can do so later from the Settings menu.

### Test Words
lingoBoost was developed to integrate with foreign language learning applications, so testing vocabulary performance on a regular basis is a must! Test Session will retrieve all of the words the user has trained on, to date, and test them with a basic recall test (stimulus = foreign word, response = user's English translation).  Users must first set a preferred volume following the on-screen instructions before starting the test.

### Sleep Session
Sleep session is the core of the app, providing a "TMR Lite" experience.  This portion of the app should be run when users are ready for bed.  Users will be prompted to select the volume for the white noise and spoken words/sounds to ensure that words/sounds are audible through the white noise. After pressing *READY FOR SLEEP*, a popup (Android Toast) will briefly let the user know that the words (or sounds) have been updated from the server.

## Global Parameters (Langlearn-API)
These are parameters that are passed from the server to the client, using the langlearn-api (separate, private repo). 
The keys and default values (global) are listed below. These values are managed from the admin page through the API so that they can be overridden at the user level, so lingoBoost may see values that differ from those below.

| Key | Default Value | Description |
| --- | ------------- | ----------- |
| word_delay | 10	| Time, in seconds, that should elapse between each word stimulation |
| random | True	| Boolean; If True, the order of words will be randomized |
| feedback | True | Boolean; If True, correct translations will be shown during test, after each response |
| sham | False | Boolean; If True, no word stimulation will take place |
| min_words | 10 | The number of words that must have been learned/practiced since the time of the client request in order for the server to return anything, otherwise the server returns "Not enough words practiced." The client may request 1) all words, 2) words since a given date-time, or 3) words this calendar day (server-based calendar). |
| volume_dampening | 30 | Percent (integer) of the user-set volume we want white noise to decrease to, during word presentation; dampened_volume = (volume_dampening/100)\*user_WhiteNoise_Volume |
| stimulation_stop | 18000 | Time, in seconds, when stimulation should **stop** after the Sleep Session began |
| session_start_delay | 1800	| Time, in seconds, that should elapse after beginning a sleep session before stimulation starts |
| pause_delay | 300	| Time, in seconds, that should elapse after resuming a sleep session after a pause |
| all_words | True	| Boolean; If True, Duolingo users will have all the words they have ever trained sent during the Sleep session (recent\*3 + old) |

## Uploading Word Lists 
Format: comma-delimited with word wav file, matched wav file (semantic), and word. No header!
If no "matched" sound is needed, it can be ignored. See the following example;

ld1.wav,,lucid1

ld2.wav,,lucid2

ld3.wav,,lucid3

ld4.wav,,lucid4

## Data!
We have glorious data output links on the admin page that will output csv's for both the test logs and sleep logs (Test Summary, Sleep Summary).

The examples and lists below represent the current setup. **Two new variables will be added in subsequent test logs. These variables represent test length, so that we can easily determine if a test log is complete, and whether a test session is actually the pre (first test, prior to training) or post (final test, after all test and training) tests.

### Time
All timestamps have been converted to match the user's local time. All the durations are represented in seconds.

### Test Summary
#### Headers
nickname,log_type,session_hash,session_start,session_end,session_duration,log_event_time,trained_word,test_response,closest_translation,string_score,duolingo_first_practiced

#### Example
corticalre,test,8a46affe-c6e9-48bb-b4ac-dde3a6475a12,2017-10-28 20:43:04-04:00,2017-10-28 20:57:20-04:00,856.0,2017-10-28 20:43:22-04:00,nötkött,beef,beef,1.0,2017-10-27 21:32:03-04:00

### Sleep Summary
#### Headers
nickname,log_type,session_hash,session_start,session_end,session_duration,log_event_time,trained_word,system_volume,white_noise_volume,word_volume

##### Example
corticalre,sleep,1b19955a-1cf2-4ced-a173-78450153e600,2017-10-20 14:01:19-07:00,2017-10-20 14:02:31-07:00,72.0,2017-10-20 14:01:30-07:00,saltet,80,18,45

# Development
* Android Studio 3
** Kotlin plugin
