# Sage - Plant Browsing Android App

**Sage** is a calming digital space,  an intuitive Android application for discovering and exploring a variety of plants for sale. 

## Core Features

- **Category-Based Browsing**  
  Explore plants by category: *Indoor*, *Flowering* and *Edible*

- **Live Search with Suggestions**  
  A responsive search bar with real-time dropdown suggestions and live RecyclerView filtering.

- **Favourites System**  
  Users can save plants to their favourites and revisit them anytime.

- **Top Picks Section**  
  Shows the 3 most viewed plants to highlight popular plants.

- **Detailed Plant Information**  
  Key details on each plant including care tips, price, description, and a carousel of images.

- **User Authentication**  
  Sign up /log in, and manage account settings via Firebase Authentication.


## Little Touches

- **Dynamic Filter Highlighting**  
  When a category filter is active in the shop, the filter icon turns green to indicate it's applied.

- **Skippable Login**  
  Login isn't forced at launch. Users are only prompted to sign in when they try to access protected features like favourites

- **Single Toggle for Login/Sign-Up**  
  The login screen includes a toggle to switch between login and sign-up, and cleverly reuses that same TextView for showing error messages to reduce clutter.

- **Responsive Layouts**  
  Layouts are optimised to work smoothly on most devices.

- **No Results Handling**  
  Search gracefully handles empty queries by displaying a “No results found” message in the dropdown.

- **View Count Tracking**  
  Each time a plant is viewed, a counter is incremented in Firestore to support features like *Top Picks*.

## Tech Stack

- **Language:** Java
- **Framework:** Android SDK
- **Database:** Firebase Firestore
- **Authentication:** Firebase Auth
- **Image Loading:** Glide
- **Build Tool:** Gradle
