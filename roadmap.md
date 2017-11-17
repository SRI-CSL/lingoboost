# Roadmap for what?
lingoBoost is an app (frontend and backend) that was developed to support Targeted Memory Reactivation (TMR) for Second Language Learning. TMR is the presentation of learning cues during SWS to bias memories to consolidate/strength the memory trace for those particular items.  The current instantiation integrates with a popular mobile language learning application and presents the cues to users during a sleep session at night.  We are working on making this support other applications, like taking in cue lists based on our collaborators needs, and will be talking with a hardware company that does sleep tracking to serve as the cueing vehicle for our system in a more sophisticated setup.

# Motivations
There are motivating factors from the points of view of;
+ current usability that would really enhance what we are doing today, and 
+ make our tools and efforts more 'professionally palatable' to transition partners, and
+ provide enhanced functionality for continued research.

Almost (if not all) of these tasks are listed as issues across the langlearn and langlearn-api repos. Most of the tasks are listed for the client mobile application, with a few reserved for the backend API.  I have the target listed for each task as to whether it's for the app or API. 

# Potential Changes: Task Breakdowns
## Enhanced Functionality: Important Now
These are features that would benefit us tremendously now, but have not been implemented yet due to time and resource constraints. All of these are potentially, **individually** fixable by the end of the project, but we have to choose our tasks based on resource availability and impact.

### *App*: Add Test Resume Function
**Estimated Effort: MODERATE**

Right now, if someone exits the app, the test progress is uploaded and they would have to potentially start all over. We should have some sort of "Would you like to resume your previous test?" option.

### *App*: Intelligent Sound Adjustments
**Estimated Effort: MODERATE-HIGH**

The app is currently causing arousals later in night, and we really need a smart way of adjusting volume over time. Potential quick fixes include stopping stimulation earlier, or reducing volume over time. More complicated fixes include training a user to set the volume (onerous on the user, but potentially better long-term compliance), automatically adjusting volume based on mic-assessed ambient noise (automatic, but engineer-intensive) and even by using a combination of ambient noise characteristics with user-based distance based on spoken word activation.

### *App*: Mobile Client UI
**Estimated Effort: MODERATE-HIGH**

*Update 2017.11.16: This has been moved up in importance to add a certain level of face validity and polish for future transition.* To be instep with the bar for any modern mobile application, we could change the general interface to feel like a proper, developed app (e.g., Twitter, Gmail, Hulu, Strava...)

***

## Critical Functionality: Important @ Scale and Distant Future Need
These are features that would be absolutely critical should we scale. However, the benefit in the short term, given the potential large work requirement, likely does not make sense.

### *API*: Access Restriction for users and groups
**Estimated Effort: HIGH**

Right now, any admin user can see/edit everything! We definitely need a way to limit functionality so people do not accidentally mess with other folks settings.

### *App*: Cross-Platform Compatibility
**Estimated Effort: HIGH**

Currently, we are only Android and we definitely need iOS support.

***

## Enhanced Functionality: Important @ Scale and Distant Future Need
These are amazing future features that would be fantastic, but realistically are unlikely to be completed given the remainder of the project (2017).

### *App*: Integrate as Background Media Player
**Estimated Effort: MODERATE-HIGH**

*Update 2017.11.16: This was down-graded from immediate need to future need. Many apps that monitor at this level require being foreground, and this likely doesn't provide a material benefit.*
Right now we need to be a foreground app, but we should seriously consider what is required to allow the app to run in the background while maintaining correct stimulation (white noise only while user is alert/active) so that the sound is more like a media service and operates in a more integrated manner with the OS.

### *App, API*: Intelligent Sleep Detection/Prediction
**Estimated Effort: HIGH**

Integrate user input and behavioral patterns to modulate stimulation timing.
This is likely a pretty large endeavor, of course. This could be simpler or more complicated.
Simpler: Take in some user input and try and stimulate based on our parameters with some input from their weekly sleep schedule.
Complicated: Learn about their actual usage patterns and incorporate that information, ala Nest.

Faust: [sleep as android](https://sleep.urbandroid.org/) is an app that we could use as sleep phase detector.  It's already set up to publish intents (messages) that we could listen to and use as stimuli triggers (one of them is intended as a lucid dream trigger), and it supports a bunch of wearables.  Interestingly, they don't offer sleep detection and they give an [explanation](https://sleep.urbandroid.org/automatic-fall-asleep-detection/) of why they don't.

### *App*: Intelligent Sound Generation 
**Estimated Effort: MODERATE-HIGH**

Incorporate numerous types of sleep-focused background noises that we can use that make the app more in line with all of the popular white noise machines (physical [DOHM], digital, or app-based). e.g., waves and such

***

## Enhanced Usability: Moderate Importance @ Scale
These are features that would be great to have at scale, but our current solutions (and hacks) are getting the job done well enough for now.

### *App*: Add "Check Status" to App
**Estimated Effort: MODERATE**

Right now users need to exit the app and use our web interface to check their status. This should really be included in the app/client to allow users to check their history/status.

### *App*: Gap in Seamless Audio: Procedurally-Generated Sound
**Estimated Effort: MODERATE**

Simply looping a wav file works right now, but it's tremendously limiting and leads to some gap issues due to weird audio handling in particular combinations of Android OS and Hardware. We should be able to procedurally generate the sound information.

### *App*: User Interface Updates for Stimulus Control
**Estimated Effort: MODERATE**

We could allow for finer grained control over the volume/pause/resume for both white noise and stimulation overnight for users. This could lead to a better UX, but could also just be a stop-gap solution towards a more longer-term automated solution.

### *API-Web*: Research Dashboard UI
**Estimated Effort: MODERATE**

The admin interface is functional, but may not seem professional to transition clients. It would be great to make the login and subsequent usable components more UI focused.
