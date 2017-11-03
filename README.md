 <!--- Banner Hotlink Test ---> 
![lingoBoost](http://cortical.csl.sri.com/images/lingoBoost_banner_v2.png)

## Starting Up
When users first start the lingoBoost app, they should enter the username that is assigned by the researcher.
![startup screen](https://user-images.githubusercontent.com/6577692/32380349-1c5d7450-c06d-11e7-9748-d1a0bad3613f.png)

If users need to adjust their username, they can do so later from the Settings menu.

## Test Words
lingoBoost was developed to integrate with foreign language learning applications, so testing vocabulary performance on a regular basis is a must! Test Session will retrieve all of the words the user has trained on, to date, and test them with a basic recall test (stimulus = foreign word, response = user's English translation).  Users must first set a preferred volume following the on-screen instructions before starting the test.

## Sleep Session
Sleep session is the core of the app, providing a "TMR Lite" experience.  Users will be prompted to select the volume for the white noise and spoken words/sounds to ensure that words/sounds are audible through the white noise. After pressing *READY FOR SLEEP*, a popup (Android Toast) will briefly let the user know that the words (or sounds) have been updated from the server.

## Global Parameters
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


# Development
* Android Studio 3
** Kotlin plugin
