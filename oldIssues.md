Intelligent Sleep Detection/Prediction #53
Right now our stimulation triggers and timers are all based on a simple "sleep session onset" timepoint and calculated based on elapsed time from there.
We could/should integrate user input and behavioral patterns to try and stimulate at the right time. This is likely a pretty large endeavor, of course. This could be simpler or more robust.
Simpler: Take in some user input and try and stimulate based on our parameters with some input from their weekly sleep schedule.
More Complicated: Learn about their actual usage patterns and incorporate that information, ala Nest.
This pushes the app towards a more integrated "lifestyle" feature that may work it's way into next-generation operating systems (similar to f.Lux and night shift for blue light).

Intelligent Sound Generation #52
Rather than just a simple White Noise sound, we should incorporate numerous types of sleep-focused background noises that we can use that make the app more in line with all of the popular white noise machines (physical [DOHM], digital, or app-based).
e.g., waves and such
This pushes the app towards a more integrated "lifestyle" feature that may work it's way into next-generation operating systems (similar to f.Lux and night shift for blue light).

Integrate as Background Media Player #50
Currently the app requires that it is in the foreground, which is pretty standard for this sort of application because the app cannot access certain components, like accelerometers or other system events, while it is a background service.
However, for this to function properly, it would need to allow the stimulation to act as a background service, where if the app goes to the background, stimulation pauses but the white noise plays in the background.
It would also be ideal for this to be controllable through the notifications like other modern media players on Android.

Intelligent Sound Adjustments #49
The app sounds are causing arousals over night and this is a fairly critical issue that is causing user complaints. This could either be caused by word stimulation or white noise (more data is needed).
We could simply decrease sound gradually over night, or stop stimulation earlier in night (e.g., target first 2 cycles of SWS rather than 4 cycles).
One potential solution is to occasionally use the mic to adjust sound levels based on background noise. We could do this fully automatically, or by modulating user settings. E.g., Modulation: If a user sets volume when the mic picks up a lot of background noise, then it should decrease volume later if the mic determines that the background noise has ceased.

Cross-Platform Compatibility #48
We need to support iOS/iPhone because of the huge user base. Should this be a dedicated platform-specific solution, or is it possible to do something in a shared cross-platform environment?

Add Test Resume Function #46
When a test is not complete, but a user exits or leaves the app, it should automatically save the state of the test so that the user may choose to resume the test later or start a new test.

The next time a user selects "Test Words" the app should prompt the user with:
"You did not complete your last test session that ended at <last_event_time_in_file>. Would you like to Resume?" Then with two button options: Resume Test; Start New Test

If the user selects "Start New Test" it should upload the previous saved test file and then query the server for the most recent data.
If the user selects "Resume" then it should append the data to the previous file (retaining the session_hash) and mark the log with a 'event.test_resume' event.

In the future, we may want to push notifications for paused tests.

Add "Check Status" to App #45
We should enable the app to display the basic Status information inside the app, rather than forcing people to use a web browser.
Currently it's a very simple text based status page (https://lingoboost.csl.sri.com/lingoturk/user/corticalre/status), so I imagine that future integration should be simple enough, with potential for better UI updates in the future for enhanced usability and visualization.

Gap in Seamless Audio: Procedurally-Generated Sound #42
After some testing and debugging, it seems that it's nigh impossible to achieve truly seamless audio playback on a range of Android devices. There are reddit complaints, stack overflow issues, 2, and a claim that 6.0.1 just has this problem even when you fix it in the most general way possible.

The likely "correct" solution is to dynamically generate white noise (ala, /pmarks-net/chromadoze/). However, I don't know how long that sort of implementation would take.
