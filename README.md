# Tour App

## Demo Video

https://www.youtube.com/watch?v=MLkRKMfC6gA

## Screenshots

https://imgur.com/a/nwfsU9b

## Inspiration

Geographic illiteracy is a serious issue in the United States (and other places around the world as well), especially since the topic is frequently eliminated from school curricula and combined or replaced with other topics, such as social studies or economics. According to some, this knowledge gap may pose a security threat, as it is important for even the average citizen to be able to pinpoint the locations of important places within their country or countries that they are at war with, for example. For a country to be successful, especially in democratic governments, it is important for the citizens who live in it to be more geographically aware.

Sources:
https://www.usnews.com/news/articles/2015/10/16/us-students-are-terrible-at-geography
https://asiatimes.com/2020/01/us-geographic-illiteracy-may-be-security-threat/

## What it does

This app teaches its users about the places that they go, as they go there! It works as a hands free app in the background, so all the user has to do is turn it on and it will retrieve and read information from Wikipedia as new cities are entered. The user can customize certain settings, such as if a sound is played before narration, if only the city name is announced, or set a threshold of how many words to read from the Wikipedia article. Users can see their previous locations from the current session on a map in the home screen.

## How we built it

We built this app using Android Studio, the Google Maps API (for the home screen map), and the Wikipedia article retrieval API. We also used the built-in Geocoding and Text-to-Speech Android APIs.

## Challenges we ran into

A significant challenge we ran into was deciding which type of Android service to run, but we eventually decided that a foreground service was best for our app because it would not automatically be killed by the operating system as easily as a background service. We also ran into a few challenges and bugs concerning the activity and process lifecycle, such as accidentally setting up duplicate event listeners and the app not functioning properly after being restarted on the device (which we fixed within the last few hours).

## Accomplishments that we're proud of

- In the process of making the app, we became better Android developers and better developers in general
- At the end of the hackathon we had no features that were unimplemented or lacking functionality and no bugs whatsoever (that we could see)!
- This was the first time any of us used the Wikipedia API

## What we learned

- How to run a foreground service
- How to use Volley to request websites
- Location event listeners

## What's next for Tour App

- Keeping a history of all the different cities that a user has entered, in all runs of the app.
- Geofencing to get nearby tourist attractions and points of interest
- Supporting all of the countries in the world

## Resource Credits
- [city.svg](https://www.flaticon.com/free-icon/city_3310572?term=city%20location&page=2&position=74&page=2&position=74&related_id=3310572) - Created by ultimatearm, hosted on flaticon.com
- [square.svg](https://www.flaticon.com/free-icon/square_2627936?term=square&page=1&position=14&page=1&position=14&related_id=2627936&) - Created by Vitaly Gorbachev, hosted on flaticon.com
- [settings.svg](https://www.flaticon.com/free-icon/gear_1242494?term=settings&page=1&position=51&page=1&position=51&related_id=1242494) - Created by Freepik, hosted on flaticon.com
- [Airplane-call-bell-sound.mp3](https://orangefreesounds.com/airplane-call-bell-sound/) - Created by Alexander, hosted on OrangeFreeSounds; CC By-NC 4.0
