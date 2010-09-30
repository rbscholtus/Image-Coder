# Image Coder

Image Coder is an easy-to-use sandbox that helps students to play around with pixel-based image manipulation using Java code, and study graphics-related concepts. The main idea of Image Coder is that you write a snippet of Java code and apply it directly to an image.

## Running Image Coder

The easiest way to run Image Coder is to go to the application [[Launcher page|https://mekong.rmit.edu.vn/~v00132/imagecoder/]]. Image Coder requires a Java runtime environment to be installed do your computer.

Alternatively, you can download the latest binary from the downloads page. However this requires additional libraries to be installed (see below.) I do not distribute those libraries here. The libraries have to be copied to a lib subfolder in the folder where the main binary is.

## Downloading the source

Everyone is free to download the source code and contribute to it. The URL for downloading the source is near the top of this page.

After downloading the source, you have to download the additional libraries (see below.) The libraries have to be copied to the ./lib folder in the NetBeans project folder.

## Additional libraries

Compiling and running Image Coder yourself requires Javassist, TableLayout and TableLayoutBuilders.

* Javassist
  * Download Javassist from [[jboss.org|http://www.jboss.org/javassist/downloads.html]]
  * Choose a recent version. Version 3.11.0 GA works fine. The downloaded file is `javassist-3.11.GA`
  * Extract `javassist.jar` from the zip archive and put it in `lib`.

* TableLayout and TableLayoutBuilders
  * You can find these on [[java.net|https://tablelayout.dev.java.net/]]
  * On the downloads page, choose `Binary Jdk 1.5` and `Builders`
  * Rename the downloaded files to `TableLayout.jar` and `TableLayoutBuilders.jar`
  * Put them in `lib`.
