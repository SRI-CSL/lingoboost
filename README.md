# LingoBoost 

## Global Parameters
These are parameters that are passed from the server to the client. The value and default global value is provided. These values can be overridden at the user level, so that the client may see values that differ from those below.

| Key | Default Value | Description |
| --- | ------------- | ----------- |
| word_delay | 10	| Time, in seconds, that should elapse between each word stimulation |
| start_delay | 1800	| Time, in seconds, that should elapse after a beginning or resuming a sleep session before stimulation starts |
| random | True	| Boolean; If True, the order of words will be randomized |
| feedback | True | Boolean; If True, correct translations will be shown during test, after each response |
| sham | False | Boolean; If True, no word stimulation will take place |
| min_words | 10 | The number of words that must have been learned/practiced since the time of the client request in order for the server to return anything, otherwise the server returns "Not enough words practiced." The client may request 1) all words, 2) words since a given date-time, or 3) words this calendar day (server-based calendar). |
| volume_dampening | 30 | Percent (integer) of the user-set volume we want white noise to decrease to, during word presentation; dampened_volume = (volume_dampening/100)\*user_WhiteNoise_Volume |
| stimulation_stop | 18000 | Time, in seconds, when stimulation should **stop** after the Sleep Session began |
