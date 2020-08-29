## An introduction
This is an app initially built with an aim to address the issue of mental health and providing a platform to all the Bitsians so as to help express themselves
to other fellow bitsians anonymously especially in these times and get some moral help in return.The app offers all the chat features a normal 
chatting application would offer along with "Helper" and "Seeker" tags to users so as to distinguish them and create a safe haven for the "Seekers" to express themselves.

## Challenges faced
Just as the app was about to get finished,i realized cloud firestore,instead of real time database, would suit my app better by offering offline
caching and returning faster queries.To structure the code,to migrate from 
rtd to cloud firestore and then to re-write the code again so that it suited firestore was quite tedious and required a lot of patience to get through.   
 
## Tech stack & Open-source libraries

Kotlin based 

JetPack\
 LiveData - notify domain layer data to views.\
 Lifecycle - dispose of observing data when lifecycle state changes.\
 ViewModel - UI related data holder, lifecycle aware.

Architecture\
 MVVM Architecture

Picasso - A powerful image downloading and caching library.\
Material-Components - Material design components.\
Groupie-Library for complex RecyclerView layouts.
