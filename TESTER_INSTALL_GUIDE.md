# Installing Messier Tonight (Beta Test Build, v2)

Thanks for taking a look! This is a test version of an Android app I built —
it tells you what's worth looking at tonight (Messier objects, NGC objects,
and visible planets), based on your location and the current time.

Already have v1 installed? No need to uninstall it first — this installs
right over it, and your favorites/filter notes carry over automatically.

This isn't on the Play Store yet, so installing it looks a little different
than a normal app. It's completely safe — Android just doesn't know this
app yet, since it didn't come from the Play Store. Takes about 2 minutes.

## What you need

- An **Android phone** (this won't work on an iPhone)
- The `.zip` file this guide came in, with `app-debug.apk` inside it

## Steps

1. **Unzip the file** on your phone. If your phone doesn't unzip files
   automatically, any file manager app (or the built-in "My Files" /
   "Files" app) can do it — tap the zip, then "Extract" or "Unzip."

2. **Tap `app-debug.apk`** inside the unzipped folder.

3. Android will show a warning like *"For your security, your phone is
   not allowed to install unknown apps from this source."* This is
   expected — tap **Settings** on that popup, then turn on
   **Allow from this source**. This is a one-time permission for whichever
   app you used to open the file (Files, My Files, etc.).

4. Go back and tap `app-debug.apk` again → **Install** → **Open**.

5. On first launch, the app will ask for **location permission** — allow
   it (either "While using the app" or "Precise" works fine). This is how
   it knows where you are so it can calculate what's visible in your sky.
   If you say no, it'll just default to New York City instead.

That's it — you're in.

## A couple things to know

- This is a **debug build**, not a polished release — you may hit rough
  edges. That's exactly what I'm hoping to find out about.
- It needs an internet connection the first time you search for a
  location by name, or if you check your elevation — GPS location itself
  works offline.
- To uninstall later: press and hold the app icon → **Uninstall**, same as
  any other app.

## Feedback

Post whatever you find in the Discord — bugs, confusing bits, features you
wish it had, sort options you like/don't like, whether the times/altitude
numbers look right for your location. All of it is useful, even "this
crashed" with no other details.

Thanks for helping test this out!
