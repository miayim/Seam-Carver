import java.util.ArrayList;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

// represents the promised functionality that any implementation of IPixel can do
interface IPixel {
  // returns the brightness of this IPixel
  double calcBrightness();

  // returns the energy of this IPixel based on its neighbors' brightness
  double calcEnergy();

  // draws this IPixel by starting a new row in the given image (calling
  // drawPixelRow on its south neighbor), representing a
  // new row of pixels less than image height
  void drawPixelImage(ComputedPixelImage image, int x, int y, int width, int height);

  // sets this IPixel's color as a pixel on a given image, and calls
  // drawPixelImage on its east neighbor
  void drawPixelRow(ComputedPixelImage image, int x, int y, int width, int height);

  // draws this IPixel by starting a new row in the given image (calling
  // drawPixelRow on its south neighbor), representing a
  // new row of pixels less than image height
  void drawPixelImageGray(ComputedPixelImage image, int x, int y, int width, int height,
      double maxEnergy);

  // sets this IPixel's color in GrayScale based on pixel energy as a pixel on a
  // given image, and calls drawPixelImage on its east neighbor
  void drawPixelRowGray(ComputedPixelImage image, int x, int y, int width, int height,
      double maxEnergy);

  // returns possible candidates for the lowest SeamInfos going horizontally,
  // based on this IPixel's three northeast, east, and south east neighbors,
  // connected from this IPixel
  ArrayList<SeamInfo> findHorizontalSeamThreeNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams);

  // returns an AL<SeamInfo> representing a column of SeamInfos spanning the width
  // of the image in an Image
  ArrayList<SeamInfo> findHorizontalSeamCol(ArrayList<SeamInfo> finalRow);

  // returns possible candidates for the lowest SeamInfos going vertically, based
  // on this IPixel's three upper neighbors, connected from this IPixel
  ArrayList<SeamInfo> findVerticalSeamThreeNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams);

  // returns possible candidates for the lowest SeamInfos going either vertically
  // or horizontally, based on this IPixel's two neighbors, connected from this
  // IPixel
  ArrayList<SeamInfo> findSeamTwoNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams);

  // returns an AL<SeamInfo> representing a row of SeamInfos spanning the height
  // of the image in an Image
  ArrayList<SeamInfo> findVerticalSeamRow(ArrayList<SeamInfo> finalRow);

  // removes this IPixel vertically from the well-connected Graph depending the
  // given APixel
  void removeVerticalSeam(APixel prev);

  // removes this IPixel horizontally from the well-connected Graph depending the
  // given APixel
  void removeHorizontalSeam(APixel right);

  // reinserts this IPixel from the well-connected Graph
  void reinsert();

  // fixes up this IPixel's horizontal neighbors
  void fixVerticalBorder();

  // fixes up this IPixel's vertical neighbors
  void fixHorizontalBorder();
}

// represents any pixel with a color and 4 pixel neighbors in the cardinal directions
abstract class APixel implements IPixel {
  Color color;
  // edges from this node
  APixel north;
  APixel east;
  APixel south;
  APixel west;

  // EFFECT: sets this pixels neighbors to itself in order to preserve connectivity
  APixel(Color color, APixel north, APixel east, APixel south, APixel west) {
    this.color = color;
    this.north = north;
    this.east = east;
    this.south = south;
    this.west = west;

    north.south = this;
    east.west = this;
    south.north = this;
    west.east = this;
  }

  // makes a self connected APixel
  APixel(Color color) {
    this.color = color;
    this.north = this;
    this.east = this;
    this.south = this;
    this.west = this;
  }

  // returns the brightness of this APixel
  public double calcBrightness() {
    return ((this.color.getRed() + this.color.getGreen() + this.color.getBlue()) / 3) / 255.0;
  }

  // default case: APixel has no energy
  // calculates this pixel's energy based on the brightness of its neighbors
  // overrides APixel calcEnergy()
  public double calcEnergy() {
    double north = this.north.calcBrightness();
    double south = this.south.calcBrightness();
    double west = this.west.calcBrightness();
    double east = this.east.calcBrightness();
    double northwest = this.north.west.calcBrightness();
    double southwest = this.south.west.calcBrightness();
    double northeast = this.north.east.calcBrightness();
    double southeast = this.south.east.calcBrightness();

    double horizEnergy = (northwest + 2 * west + southwest) - (northeast + 2 * east + southeast);
    double vertEnergy = (northwest + 2 * north + northeast) - (southwest + 2 * south + southeast);

    return Math.sqrt(Math.pow(horizEnergy, 2) + Math.pow(vertEnergy, 2));
  }

  // returns possible candidates for the lowest SeamInfos , based on this APixel's
  // northeast, east, and southeast neighbors, connected from this APixel
  public abstract ArrayList<SeamInfo> findHorizontalSeamThreeNeighbors(
      ArrayList<SeamInfo> seamInfoAcc, ArrayList<SeamInfo> possibleSeams);

  // returns an AL<SeamInfo> representing a column of horizontally spanning
  // SeamInfos in an Image
  public abstract ArrayList<SeamInfo> findHorizontalSeamCol(ArrayList<SeamInfo> finalRow);

  // returns possible candidates for the lowest vertically linked SeamInfos, based
  // on this APixel's
  // three upper neighbors, connected from this APixel
  public abstract ArrayList<SeamInfo> findVerticalSeamThreeNeighbors(
      ArrayList<SeamInfo> seamInfoAcc, ArrayList<SeamInfo> possibleSeams);

  // returns possible candidates for the lowest SeamInfos (either vertically or
  // horizontally), based on
  // this APixel's
  // two neighbors, connected from this APixel
  public abstract ArrayList<SeamInfo> findSeamTwoNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams);

  // returns an AL<SeamInfo> representing a row of vertically spanning SeamInfos
  // in an Image
  public abstract ArrayList<SeamInfo> findVerticalSeamRow(ArrayList<SeamInfo> finalRow);

  // removes this APixel from the well-connected Graph depending the given APixel
  public abstract void removeVerticalSeam(APixel prev);

  // removes this APixel from the well-connected Graph depending the given APixel
  public abstract void removeHorizontalSeam(APixel right);

  // removes this APixel from the well-connected Graph
  public abstract void reinsert();

  // fixes up this APixel's horizontal neighbors
  public abstract void fixVerticalBorder();

  // fixes up this APixel's vertical neighbors
  public abstract void fixHorizontalBorder();

}

// represents a sentinel-like border pixel
// BorderPixels surround a grid of Pixels on all sides
class BorderPixel extends APixel {

  BorderPixel(APixel north, APixel east, APixel south, APixel west) {
    super(Color.black, north, east, south, west);
  }

  BorderPixel() {
    super(Color.black);
  }

  public void drawPixelImage(ComputedPixelImage image, int x, int y, int width, int height) {
    // does nothing, since we do not want to render this BorderPixel
  }

  public void drawPixelRow(ComputedPixelImage image, int x, int y, int width, int height) {
    // does nothing, since we do not want to render this BorderPixel
  }

  public void drawPixelImageGray(ComputedPixelImage image, int x, int y, int width, int height,
      double maxEnergy) {
    // does nothing, since we do not want to render this BorderPixel
  }

  public void drawPixelRowGray(ComputedPixelImage image, int x, int y, int width, int height,
      double maxEnergy) {
    // does nothing, since we do not want to render this BorderPixel
  }

  // returns possibleSeams, the accumulated list of possible seamInfos, since this
  // means we have traversed through a column of APixels
  public ArrayList<SeamInfo> findHorizontalSeamThreeNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams) {
    return possibleSeams;
  }

  // returns finalRow, since this method call means we are at the rightmost edge
  // of the
  // Graph and should stop searching eastward.
  public ArrayList<SeamInfo> findHorizontalSeamCol(ArrayList<SeamInfo> finalRow) {
    return finalRow;
  }

  // returns possibleSeams, the accumulated list of possible seamInfos, since this
  // means we have traversed through a row of APixels
  public ArrayList<SeamInfo> findVerticalSeamThreeNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams) {
    return possibleSeams;
  }

  // returns possibleSeams, the accumulated list of possible seamInfos, since this
  // means we have traversed through a row of APixels
  public ArrayList<SeamInfo> findSeamTwoNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams) {
    return possibleSeams;
  }

  // returns finalRow, since this method call means we are at the bottom of the
  // Graph and should stop searching downward.
  public ArrayList<SeamInfo> findVerticalSeamRow(ArrayList<SeamInfo> finalRow) {
    return finalRow;
  }

  public void removeVerticalSeam(APixel prev) {
    // does nothing, since we never want to remove a border pixel

  }

  public void removeHorizontalSeam(APixel right) {
    // does nothing, since we never want to remove a border pixel

  }

  // fixes up this border pixel's neighbors, by "removing" this pixel horizontally
  public void fixVerticalBorder() {
    this.west.east = this.east;
    this.east.west = this.west;
  }

  // fixes up this border pixel's neighbors, by "removing" this pixel vertically
  public void fixHorizontalBorder() {
    this.north.south = this.south;
    this.south.north = this.north;
  }

  public void reinsert() {
    // does nothing, since we never want to reinsert a border pixel

  }

}

// represents a pixel of a completed image with a color and four neighbors in 
// the cardinal directions
class Pixel extends APixel {

  Pixel(Color color, APixel north, APixel east, APixel south, APixel west) {
    super(color, north, east, south, west);
  }

  Pixel(Color color) {
    super(color);
  }

  // finds the minimum of the first three values of the accumulated list of
  // SeamInfo, representing the SeamInfos from this Pixel's northeast, east, and
  // southeast neighbors,
  // and creates this Pixel's seamInfo based on that minimum result.
  // continues traversing eastwards, ultimately returning an AL<SeamInfo>
  // representing
  // all possible seamInfos from this Pixel's row.
  public ArrayList<SeamInfo> findHorizontalSeamThreeNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams) {

    ArrayList<SeamInfo> threeNeighbors = new ArrayList<SeamInfo>();

    threeNeighbors.add(seamInfoAcc.get(0));
    threeNeighbors.add(seamInfoAcc.get(1));
    threeNeighbors.add(seamInfoAcc.get(2));

    SeamInfo min = new Utils().findMin(threeNeighbors);

    possibleSeams.add(new SeamInfo(this, this.calcEnergy() + min.totalWeight, min));

    seamInfoAcc.remove(0);

    if (seamInfoAcc.size() <= 2) {
      return this.south.findSeamTwoNeighbors(seamInfoAcc, possibleSeams);
    }
    else {
      return this.south.findHorizontalSeamThreeNeighbors(seamInfoAcc, possibleSeams);
    }

  }

  // begins traversing through a row to return the AL<SeamInfo> representing
  // all possible seamInfos. method is recursively called on this Pixel's east
  // neighbor,
  // ultimately returning the AL<SeamInfo> of Graph's rightmost column, with each
  // SeamInfo spanning from the leftmost side of the graph to the rightmost
  public ArrayList<SeamInfo> findHorizontalSeamCol(ArrayList<SeamInfo> finalRow) {

    // avoid aliasing
    ArrayList<SeamInfo> finalRowCopy = new ArrayList<SeamInfo>();

    for (int i = 0; i < finalRow.size(); i += 1) {
      finalRowCopy.add(finalRow.get(i));
    }

    // condition called when we have one more pixel to find a seam for before
    if (finalRowCopy.size() <= 2) {

      ArrayList<SeamInfo> initPossibleSeam = this.findSeamTwoNeighbors(finalRowCopy,
          new ArrayList<SeamInfo>());

      return initPossibleSeam;

    }
    else {

      ArrayList<SeamInfo> initPossibleSeam = this.findSeamTwoNeighbors(finalRowCopy,
          new ArrayList<SeamInfo>());

      ArrayList<SeamInfo> next = this.south.findHorizontalSeamThreeNeighbors(finalRowCopy,
          initPossibleSeam);

      return this.east.findHorizontalSeamCol(next);
    }

  }

  // finds the minimum of the first three values of the accumulated list of
  // SeamInfo, representing the SeamInfos from this Pixel's northwest, north, and
  // northeast neighbors,
  // and creates this Pixel's seamInfo based on that minimum result.
  // continues traversing eastwards, ultimately returning an AL<SeamInfo>
  // representing
  // all possible seamInfos from this Pixel's row.
  public ArrayList<SeamInfo> findVerticalSeamThreeNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams) {

    ArrayList<SeamInfo> threeNeighbors = new ArrayList<SeamInfo>();

    threeNeighbors.add(seamInfoAcc.get(0));
    threeNeighbors.add(seamInfoAcc.get(1));
    threeNeighbors.add(seamInfoAcc.get(2));

    SeamInfo min = new Utils().findMin(threeNeighbors);

    possibleSeams.add(new SeamInfo(this, this.calcEnergy() + min.totalWeight, min));

    seamInfoAcc.remove(0);

    if (seamInfoAcc.size() <= 2) {
      return this.east.findSeamTwoNeighbors(seamInfoAcc, possibleSeams);
    }
    else {
      return this.east.findVerticalSeamThreeNeighbors(seamInfoAcc, possibleSeams);
    }
  }

  // finds the minimum of the first two values of the accumulated list of
  // SeamInfo, representing the SeamInfos from this Pixel's north, and either
  // northeast or northwest neighbors,
  // and creates this Pixel's seamInfo based on that minimum result. Returns an
  // AL<SeamInfo> representing
  // all possible seamInfos with this Pixel's SeamInfo added in
  public ArrayList<SeamInfo> findSeamTwoNeighbors(ArrayList<SeamInfo> seamInfoAcc,
      ArrayList<SeamInfo> possibleSeams) {

    ArrayList<SeamInfo> twoNeighbors = new ArrayList<SeamInfo>();
    twoNeighbors.add(seamInfoAcc.get(0));
    twoNeighbors.add(seamInfoAcc.get(1));
    SeamInfo minAlt = new Utils().findMin(twoNeighbors);

    possibleSeams.add(new SeamInfo(this, this.calcEnergy() + minAlt.totalWeight, minAlt));

    return possibleSeams;

  }

  // begins traversing through a row to return the AL<SeamInfo> representing
  // all possible seamInfos. method is recursively called on this Pixel's south
  // neighbor,
  // ultimately returning the AL<SeamInfo> of Graph's bottom row, with each
  // SeamInfo spanning from the bottom of the graph to the top
  public ArrayList<SeamInfo> findVerticalSeamRow(ArrayList<SeamInfo> finalRow) {

    // avoid aliasing
    ArrayList<SeamInfo> finalRowCopy = new ArrayList<SeamInfo>();

    for (int i = 0; i < finalRow.size(); i += 1) {
      finalRowCopy.add(finalRow.get(i));
    }

    // condition called when we have one more pixel to find a seam for before
    // finishing traversal through a row
    if (finalRowCopy.size() <= 2) {

      ArrayList<SeamInfo> initPossibleSeam = this.findSeamTwoNeighbors(finalRowCopy,
          new ArrayList<SeamInfo>());

      return initPossibleSeam;

    }
    else {

      ArrayList<SeamInfo> initPossibleSeam = this.findSeamTwoNeighbors(finalRowCopy,
          new ArrayList<SeamInfo>());

      ArrayList<SeamInfo> next = this.east.findVerticalSeamThreeNeighbors(finalRowCopy,
          initPossibleSeam);

      return this.south.findVerticalSeamRow(next);
    }
  }

  // draws this Pixel's south neighbor and this Pixel's row
  public void drawPixelImage(ComputedPixelImage image, int x, int y, int width, int height) {
    if (y != height) {
      this.south.drawPixelImage(image, x, y + 1, width, height);
      this.drawPixelRow(image, x, y, width, height);
    }

  }

  // draws a row of pixels in the given image by setting this Pixel in correct
  // position, and calling this method on
  // this pixel's east neighbor
  public void drawPixelRow(ComputedPixelImage image, int x, int y, int width, int height) {
    if (x != width) {
      image.setPixel(x, y, this.color);
      this.east.drawPixelRow(image, x + 1, y, width, height);
    }
  }

  // draws this Pixel's south neighbor and this Pixel's row
  public void drawPixelImageGray(ComputedPixelImage image, int x, int y, int width, int height,
      double maxEnergy) {
    if (y != height) {
      this.south.drawPixelImageGray(image, x, y + 1, width, height, maxEnergy);
      this.drawPixelRowGray(image, x, y, width, height, maxEnergy);
    }
  }

  // draws a row of pixels in the given image by setting this Pixel's color
  // (converted to grayscale) in correct
  // position, and calling this method on
  // this pixel's east neighbor
  public void drawPixelRowGray(ComputedPixelImage image, int x, int y, int width, int height,
      double maxEnergy) {

    float gray = (float) (this.calcEnergy() / maxEnergy);

    if (x != width) {
      image.setPixel(x, y, new Color(gray, gray, gray));
      this.east.drawPixelRowGray(image, x + 1, y, width, height, maxEnergy);
    }
  }

  // given the pixel being removed to the right this pixel, correctly updates this
  // pixel's connections to the graph
  public void removeHorizontalSeam(APixel right) {
    if (this.north.east == right) {
      this.north.east = this.east;
      this.east.west = this.north;

      this.north.south = this.south;
      this.south.north = this.north;

    }
    else if (this.south.east == right) {
      this.south.east = this.east;
      this.east.west = this.south;

      this.north.south = this.south;
      this.south.north = this.north;

    }
    else {
      this.north.south = this.south;
      this.south.north = this.north;
    }
  }

  // given the pixel being removed below this pixel, correctly updates this
  // pixel's connections to the graph
  public void removeVerticalSeam(APixel below) {

    if (this.east.south == below) {

      this.east.south = this.south;
      this.south.north = this.east;

      this.east.west = this.west;
      this.west.east = this.east;

    }

    else if (this.west.south == below) {

      this.west.south = this.south;
      this.south.north = this.west;

      this.east.west = this.west;
      this.west.east = this.east;
    }

    else {

      this.east.west = this.west;
      this.west.east = this.east;
    }
  }

  public void fixHorizontalBorder() {
    // does nothing, since we never have to fix a Pixel's vertical borderPixels
    // since a pixel would not find a seam should there be |Border, Pixel, Border|
  }

  public void fixVerticalBorder() {
    // does nothing, since we never have to fix a Pixel's horizontal borderPixels
    // since a pixel would not find a seam should there be |Border, Pixel, Border|
  }

  // reinserts this pixel from a removed vertical seam given the previous APixel
  public void reinsert() {
    this.south.north = this;
    this.north.south = this;
    this.east.west = this;
    this.west.east = this;

  }
}

//represents the Wrapper class, holding a "sentinel"-like BorderPixel that is the top left 
//border pixel in the grid-like pixels
class Graph {
  int width;
  int height;
  APixel topLeft;
  ArrayList<DirectionalSeam> removed;

  Graph(int width, int height, APixel topLeft) {
    this.width = width;
    this.height = height;
    this.topLeft = topLeft;
    this.removed = new ArrayList<DirectionalSeam>();
  }

  // returns the horizontal seam of this Graph by starting off the eastward
  // traversal of pixels by ultimately finding the minimum seam from the rightmost
  // column of the pixels, which is the seamInfo that
  // is at the end of the cheapest seam in the image
  public SeamInfo findHorizontalSeam() {

    ArrayList<SeamInfo> initRow = new ArrayList<>();

    APixel curr = this.topLeft.south.east;

    for (int i = 0; i < this.height - 2; i += 1) {
      initRow.add(new SeamInfo(curr));

      curr = curr.south;
    }

    SeamInfo min = new Utils().findMin(this.topLeft.south.east.east.findHorizontalSeamCol(initRow));

    removed.add(new DirectionalSeam(min, true));
    return new Utils().makeSeamInfoCopy(min);
  }

  // returns the vertical seam of this Graph by starting off the downward
  // traversal of pixels by ultimately finding the minimum seam from the bottom
  // row of the pixels, which is the seamInfo that
  // is at the end of the cheapest seam in the image
  public SeamInfo findVerticalSeam() {
    ArrayList<SeamInfo> initRow = new ArrayList<>();
    APixel curr = this.topLeft.south.east;

    for (int i = 0; i < this.width - 2; i += 1) {
      initRow.add(new SeamInfo(curr));

      curr = curr.east;
    }

    SeamInfo min = new Utils().findMin(this.topLeft.south.south.east.findVerticalSeamRow(initRow));
    removed.add(new DirectionalSeam(min, false));
    return new Utils().makeSeamInfoCopy(min);
  }

  // given a SeamInfo, colors all pixels in the linked SeamInfos red
  public void colorSeam(SeamInfo rip) {
    rip.pixel.color = Color.red;
    while (rip.cameFrom != null) {
      rip.cameFrom.pixel.color = Color.red;
      rip = rip.cameFrom;
    }
  }

  // given a SeamInfo (whose first pixel is in the bottom row), appropriately
  // fixes up the SeamInfo's first
  // pixel's border pixels connection to the right it, decrements height, and calls a
  // recursive helper method
  // to continue appropriately "ripping" out a seam
  public void ripSeamHorizontal(SeamInfo rip) {
    APixel curr = rip.pixel;

    curr.east.fixHorizontalBorder();

    curr.north.south = curr.south;
    curr.south.north = curr.north;

    this.height -= 1;

    this.ripSeamHorizontalHelp(rip);
  }

  // appropriately "rips" out a seam by continuously calling the remove method in
  // pixel, passing in the pixel from the SeamInfo above it, while the
  // pixel from the SeamInfo above it is a Pixel.
  public void ripSeamHorizontalHelp(SeamInfo rip) {

    APixel right = rip.pixel;

    if (rip.cameFrom != null) {
      APixel left = rip.cameFrom.pixel;
      left.removeHorizontalSeam(right);
      this.ripSeamHorizontalHelp(rip.cameFrom);
    }
    else {
      right.west.fixHorizontalBorder();
    }
  }

  // given a SeamInfo (whose first pixel is in the bottom row), appropriately
  // fixes up the SeamInfo's first
  // pixel's border pixels connection below it, decrements width, and calls a
  // recursive helper method
  // to continue appropriately "ripping" out a seam
  public void ripSeamVertical(SeamInfo rip) {

    APixel curr = rip.pixel;

    curr.south.fixVerticalBorder();

    curr.west.east = curr.east;
    curr.east.west = curr.west;

    this.width -= 1;

    this.ripSeamVerticalHelp(rip);
  }

  // appropriately "rips" out a seam by continuously calling the remove method in
  // pixel, passing in the pixel from the SeamInfo above it, while the
  // pixel from the SeamInfo above it is a Pixel.
  public void ripSeamVerticalHelp(SeamInfo rip) {

    APixel below = rip.pixel;

    if (rip.cameFrom != null) {
      APixel above = rip.cameFrom.pixel;
      above.removeVerticalSeam(below);
      this.ripSeamVerticalHelp(rip.cameFrom);
    }
    else {
      below.north.fixVerticalBorder();
    }
  }

  // reinserts the last removed SeamInfo into the graph
  public void reinsert() {
    DirectionalSeam last = removed.get(removed.size() - 1);

    APixel curr = last.seam.pixel;

    // vertical
    if (!last.direction) {

      curr.south.fixVerticalBorder();

      curr.east.west = curr;
      curr.west.east = curr;

      this.width += 1;

      this.reinsertSeamHelpVertical(last.seam);

    }
    // horizontal
    else if (last.direction) {

      curr.east.fixHorizontalBorder();

      curr.north.south = curr;
      curr.south.north = curr;

      this.height += 1;

      this.reinsertSeamHelpHorizontal(last.seam);
    }
  }

  // recursively calls the reinsert method on each pixel in the given seam
  // and fixes the northern border pixel connections
  public void reinsertSeamHelpVertical(SeamInfo seam) {
    APixel below = seam.pixel;

    if (seam.cameFrom != null) {
      APixel above = seam.cameFrom.pixel;
      above.reinsert();
      this.reinsertSeamHelpVertical(seam.cameFrom);
    }
    else {
      below.north.fixVerticalBorder();
    }
  }

  // recursively calls the reinsert method on each pixel in the given seam
  // and fixes the western border pixel connections
  public void reinsertSeamHelpHorizontal(SeamInfo seam) {
    APixel right = seam.pixel;

    if (seam.cameFrom != null) {
      APixel left = seam.cameFrom.pixel;
      left.reinsert();
      this.reinsertSeamHelpHorizontal(seam.cameFrom);
    }
    else {
      right.west.fixHorizontalBorder();
    }
  }

  // returns this Graph rendered as a ComputedPixelImage
  public WorldImage render() {
    if (this.width < 0 || this.height < 0) {
      return new EmptyImage();
    }
    else {
      ComputedPixelImage newImage = new ComputedPixelImage(this.width - 2, this.height - 2);
      APixel curr = this.topLeft.south.east;

      curr.drawPixelImage(newImage, 0, 0, this.width - 2, this.height - 2);
      return newImage;
    }
  }

  // returns this Graph rendered as a ComputedPixelImage in Grayscale
  public WorldImage renderGray() {
    if (this.width < 0 || this.height < 0) {
      return new EmptyImage();
    }
    else {
      ComputedPixelImage newImage = new ComputedPixelImage(this.width - 2, this.height - 2);
      APixel curr = this.topLeft.south.east;

      curr.drawPixelImageGray(newImage, 0, 0, this.width - 2, this.height - 2,
          this.findMaxEnergy());
      return newImage;
    }
  }

  // finds the pixel with the highest energy in the image and returns that
  // pixels energy
  public double findMaxEnergy() {
    ArrayList<Double> possibleMax = new ArrayList<>();
    APixel curr = this.topLeft.south.east;
    APixel currRowStart = this.topLeft.south.east;
    double currMax = 0;

    for (int i = 0; i < this.height - 2; i += 1) {
      for (int j = 0; j < this.width - 2; j += 1) {
        if (curr.calcEnergy() > currMax) {
          currMax = curr.calcEnergy();
        }
        curr = curr.east;
      }
      possibleMax.add(currMax);
      currRowStart = currRowStart.south;
      curr = currRowStart;
    }

    double max = 0;
    for (int i = 0; i < possibleMax.size(); i += 1) {
      if (possibleMax.get(i) > max) {
        max = possibleMax.get(i);
      }
    }
    return max;
  }

  // returns true if this Graph's width or height is less than or equal to 3,
  // meaning there
  // are no more possible seams to be carved
  public boolean shouldEnd() {
    return this.width <= 3 || this.height <= 3;
  }
}

// represents a data structure consisting of a seamInfo and whether or not the associated 
// SeamInfo was removed horizontally
class DirectionalSeam {
  SeamInfo seam;
  boolean direction; // false represents vertical, true represents horizontal

  DirectionalSeam(SeamInfo seam, boolean direction) {
    this.seam = seam;
    this.direction = direction;
  }

}

// represents a linked data structure, consisting of a pixel associated with this SeamInfo, 
// an accumulated totalWeight  (the weight of the SeamInfo cameFrom in addition to the weight
// of the pixel associated with this seamInfo),
// and the linked SeamInfo that prior to this SeamInfo
class SeamInfo {
  APixel pixel;
  double totalWeight;
  SeamInfo cameFrom;

  SeamInfo(APixel pixel, double totalWeight, SeamInfo cameFrom) {
    this.pixel = pixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;

  }

  SeamInfo(APixel pixel) {
    this.pixel = pixel;
    this.totalWeight = pixel.calcEnergy();
    this.cameFrom = null;

  }
}

class SeamCarverWorld extends World {
  int width;
  int height;
  Graph pixels;
  int counter;
  SeamInfo curr;
  boolean paused;
  boolean seamDirection; // false represents vertical, true represents horizontal
  boolean grayScale; // false represents color, true represents the photo in black and white based
  // on the maximum energy

  SeamCarverWorld(int width, int height, Graph pixels, int counter, boolean paused,
      boolean seamDirection, boolean grayScale) {
    this.width = width;
    this.height = height;
    this.pixels = pixels;
    this.counter = counter;
    this.paused = paused;
    this.seamDirection = seamDirection;
    this.grayScale = grayScale;
  }

  SeamCarverWorld(int width, int height, Graph pixels) {
    this.width = width;
    this.height = height;
    this.pixels = pixels;
    this.counter = 0;
    this.paused = false;
    this.grayScale = false;
  }

  // constructor that takes in a fileImage and converts it to a Graph
  SeamCarverWorld(FromFileImage fileImage) {
    this.width = (int) fileImage.getWidth();
    this.height = (int) fileImage.getHeight();
    this.pixels = new Utils().makeConnections(new Utils().extractPixels(fileImage, width, height),
        width, height);
    this.counter = 0;
  }

  // returns the Graph rendered as a scene
  public WorldScene makeScene() {

    WorldImage title = new TextImage("SeamCarving", 24, Color.BLUE);
    WorldImage rules1 = new TextImage("- press space bar to pause seam carving", 15, Color.BLUE);
    WorldImage rulesReinsert = new TextImage(
        "- press u to reinsert the last removed seam (cannot undo on pause)", 15, Color.BLUE);

    WorldImage heading = new TextImage("When there is no colored seam on screen:", 15, Color.BLUE);
    WorldImage rules2 = new TextImage(
        "- press g to toggle between actual image pixels and the energies of each pixel, "
        + "in grayscale",
        15, Color.BLUE);
    WorldImage rules3 = new TextImage(
        "- press v for a single vertical seam removal (removed seam will not be colored) ", 15,
        Color.BLUE);
    WorldImage rules4 = new TextImage(
        "- press h for a single horizontal seam removal (removed seam will not be colored)", 15,
        Color.BLUE);

    WorldScene w = new WorldScene(1000, 1000);

    if (!this.grayScale) {
      WorldImage image = this.pixels.render();
      w.placeImageXY(image, 500, 400);
      w.placeImageXY(
          new AboveImage(title, new AboveImage(rules1, new AboveImage(rulesReinsert,
              new AboveImage(heading, new AboveImage(rules2, new AboveImage(rules3, rules4)))))),
          500, 100);
      // image.saveImage(
      // Integer.toString(this.width) + "x" + Integer.toString(this.height) +
      // "live.png");
    }

    else {
      WorldImage image = this.pixels.renderGray();
      w.placeImageXY(image, 500, 400);
      w.placeImageXY(
          new AboveImage(title,
              new AboveImage(rules1,
                  new AboveImage(heading, new AboveImage(rules2, new AboveImage(rules3, rules4))))),
          500, 100);
    }
    return w;
  }

  // returns scene showing that the game ends
  public WorldScene lastScene(String msg) {
    WorldImage image = new TextImage(msg, 24, Color.BLUE);
    WorldScene w = new WorldScene(1000, 1000);
    w.placeImageXY(image, 500, 400);
    return w;
  }

  public void onKeyEvent(String key) {
    if (key.equals(" ")) {
      this.paused = !this.paused;
    }
    // a user can rip a vertical and horizontal seam on key press,
    // the seam will not be colored
    else if (key.equals("v") && this.counter % 2 == 0) {
      this.curr = this.pixels.findVerticalSeam();

      this.pixels.ripSeamVertical(this.curr);
    }
    else if (key.equals("h") && this.counter % 2 == 0) {
      this.curr = this.pixels.findHorizontalSeam();

      this.pixels.ripSeamHorizontal(this.curr);
    }
    else if (key.equals("g") && this.counter % 2 == 0) {
      this.grayScale = !this.grayScale;
    }
    else if (key.equals("u") && this.counter % 2 == 0) {
      this.pixels.reinsert();
    }
  }

  // on tick method used for testing (passes in a random)
  public void onTickForTesting(Random rand) {
    if (this.pixels.shouldEnd()) {
      this.endOfWorld("There are no more seams to carve.");
    }
    else {
      // every two ticks, randomly choose the direction of the seam to be ripped
      if (counter % 2 == 0) {
        if (rand.nextInt() % 2 == 0) {
          this.seamDirection = false;
        }
        else {
          this.seamDirection = true;
        }
      }
      // we are removing a vertical seam
      if (!this.seamDirection) {
        // if we are not paused and on the first tick
        if (!this.paused && this.counter % 2 == 0) {
          // find and color vertical seam on first tick
          this.curr = this.pixels.findVerticalSeam();
          this.pixels.colorSeam(curr);
          this.counter += 1;
        }
        // if we are on the second tick, regardless of paused status
        else if (this.counter % 2 == 1) {
          // remove vertical seam on second tick
          this.pixels.ripSeamVertical(curr);
          this.width = this.pixels.width - 2;
          this.counter += 1;
        }
        else {
          // dont do anything since we are paused
        }
      }
      // we are removing a horizontal seam
      else if (this.seamDirection) {
        // if we are not paused and on the first tick
        if (!this.paused && this.counter % 2 == 0) {
          // find and color horizontal seam
          this.curr = this.pixels.findHorizontalSeam();
          this.pixels.colorSeam(curr);
          this.counter += 1;
        }
        // if we are on the second tick, regardless of paused status
        else if (this.counter % 2 == 1) {
          // remove horizontal seam on second tick
          this.pixels.ripSeamHorizontal(curr);
          this.height = this.pixels.height - 2;
          this.counter += 1;
        }
      }
    }

  }


  // for every two ticks, returns the minimum vertical or horizontal seam being
  // colored on the
  // first tick and removes that same minimum seam on the second tick.
  public void onTick() {
    if (this.pixels.shouldEnd()) {
      this.endOfWorld("There are no more seams to carve.");
    }
    else {

      // every two ticks, randomly choose the direction of the seam to be ripped
      if (counter % 2 == 0) {
        int rand = new Random().nextInt();
        if (rand % 2 == 0) {
          this.seamDirection = false;
        }
        else {
          this.seamDirection = true;
        }
      }
      // we are removing a vertical seam
      if (!this.seamDirection) {
        // if we are not paused and on the first tick
        if (!this.paused && this.counter % 2 == 0) {
          // find and color vertical seam on first tick
          this.curr = this.pixels.findVerticalSeam();
          this.pixels.colorSeam(curr);
          this.counter += 1;
        }
        // if we are on the second tick, regardless of paused status
        else if (this.counter % 2 == 1) {
          // remove vertical seam on second tick
          this.pixels.ripSeamVertical(curr);
          this.width = this.pixels.width - 2;
          this.counter += 1;
        }
        else {
          // dont do anything since we are paused
        }
      }
      // we are removing a horizontal seam
      else if (this.seamDirection) {
        // if we are not paused and on the first tick
        if (!this.paused && this.counter % 2 == 0) {
          // find and color horizontal seam
          this.curr = this.pixels.findHorizontalSeam();
          this.pixels.colorSeam(curr);
          this.counter += 1;
        }
        // if we are on the second tick, regardless of paused status
        else if (this.counter % 2 == 1) {
          // remove horizontal seam on second tick
          this.pixels.ripSeamHorizontal(curr);
          this.height = this.pixels.height - 2;
          this.counter += 1;
        }
      }
    }

  }
}

// houses auxiliary methods
class Utils {
  // converts a FromFileImage to a ComputedPixelImage, and then creates an
  // ArrayList<ArrayList<APixel>> representing the original pixelated image
  // surrounded by BorderPixels
  public ArrayList<ArrayList<APixel>> extractPixels(FromFileImage fileImage, int width,
      int height) {

    ComputedPixelImage pixelatedImage = new ComputedPixelImage(width, height);
    // converts file image to pixelatedImage
    for (int i = 0; i < height; i += 1) {
      for (int j = 0; j < width; j += 1) {
        Color color = fileImage.getColorAt(j, i);
        pixelatedImage.setColorAt(j, i, color);
      }
    }

    ArrayList<ArrayList<APixel>> grid = new ArrayList<ArrayList<APixel>>();
    // makes an AL<AL<APixel>> that represents the pixelated image
    // the topmost row, leftmost column, bottom most row, and rightmost column are
    // all border pixels
    // All constructed APixels in this AL<AL<APixels> refer only to themselves:
    // north/south/east/west fields point to the APixel being constructed.
    for (int i = 0; i < height + 2; i += 1) {
      ArrayList<APixel> pixelRow = new ArrayList<APixel>();
      for (int j = 0; j < width + 2; j += 1) {
        APixel pixel;
        if (i == 0 || j == 0 || i == height + 1 || j == width + 1) {
          pixel = new BorderPixel();
        }
        else {
          pixel = new Pixel(pixelatedImage.getPixel(j - 1, i - 1));
        }
        pixelRow.add(pixel);
      }
      grid.add(pixelRow);
    }
    return grid;
  }

  // iterates through the AL<AL<APixels> and
  // makes the well-formed connection for each APixel based on APixel's neighbors
  public Graph makeConnections(ArrayList<ArrayList<APixel>> grid, int width, int height) {
    for (int i = 0; i < height + 2; i++) {
      for (int j = 0; j < width + 2; j++) {
        APixel currentPixel = grid.get(i).get(j);
        // if currentPixel is a border pixel in the topmost row
        if (i == 0) {
          currentPixel.north = currentPixel;
          currentPixel.south = grid.get(i + 1).get(j);
        }
        // if currentPixel is a border pixel in the bottom most row
        else if (i == height + 1) {
          currentPixel.north = grid.get(i - 1).get(j);
          currentPixel.south = currentPixel;
        }
        else {
          currentPixel.north = grid.get(i - 1).get(j);
          currentPixel.south = grid.get(i + 1).get(j);
        }

        // if currentPixel is a border pixel in the leftmost column
        if (j == 0) {
          currentPixel.west = currentPixel;
          currentPixel.east = grid.get(i).get(j + 1);
        }
        // if currentPixel is a border pixel in the rightmost column
        else if (j == width + 1) {
          currentPixel.west = grid.get(i).get(j - 1);
          currentPixel.east = currentPixel;
        }
        else {
          currentPixel.west = grid.get(i).get(j - 1);
          currentPixel.east = grid.get(i).get(j + 1);
        }
      }
    }

    // returns a Graph with a "sentinel" access point of the bottom left most
    // borderPixel
    return new Graph(width + 2, height + 2, grid.get(0).get(0));

  }

  // returns the lowest SeamInfo (determined by totalWeight) from a list of
  // SeamInfos
  public SeamInfo findMin(ArrayList<SeamInfo> infos) {
    SeamInfo min = infos.get(0);
    for (int i = 0; i < infos.size(); i += 1) {
      if (infos.get(i).totalWeight < min.totalWeight) {
        min = infos.get(i);
      }
    }
    return min;
  }

  // makes a copy of the given seamInfo with the same connections and color
  public SeamInfo makeSeamInfoCopy(SeamInfo s) {
    SeamInfo copy = new SeamInfo(
        new Pixel(s.pixel.color, s.pixel.north, s.pixel.east, s.pixel.south, s.pixel.west),
        s.totalWeight, null);
    SeamInfo current = copy;
    SeamInfo sCopy = s;
    while (sCopy.cameFrom != null) {
      sCopy = sCopy.cameFrom;
      current.cameFrom = new SeamInfo(new Pixel(sCopy.pixel.color, sCopy.pixel.north,
          sCopy.pixel.east, sCopy.pixel.south, sCopy.pixel.west), sCopy.totalWeight, null);
      current = current.cameFrom;

    }
    return copy;
  }
}

class ExamplesSeamCarver {

  void testBigBang(Tester t) {
    SeamCarverWorld s = new SeamCarverWorld(new FromFileImage("balloons.png"));
    int worldWidth = 1000;
    int worldHeight = 1000;
    double tickRate = .1;
    s.bigBang(worldWidth, worldHeight, tickRate);
  }

  // ------ EXAMPLE PIXELS/GRAPH ------ //

  ArrayList<APixel> row0 = new ArrayList<>();

  APixel zeroZeroNW = new BorderPixel();
  APixel zeroZeroAbove = new BorderPixel();
  APixel zeroOneAbove = new BorderPixel();
  APixel zeroTwoAbove = new BorderPixel();
  APixel zeroThreeAbove = new BorderPixel();
  APixel zeroThreeNE = new BorderPixel();

  ArrayList<APixel> row1 = new ArrayList<>();

  APixel zeroZeroLeft = new BorderPixel();
  APixel zeroZero = new Pixel(new Color(142, 207, 242));
  APixel zeroOne = new Pixel(new Color(142, 207, 242));
  APixel zeroTwo = new Pixel(new Color(142, 207, 242));
  APixel zeroThree = new Pixel(new Color(142, 207, 242));
  APixel zeroThreeRight = new BorderPixel();

  ArrayList<APixel> row2 = new ArrayList<>();

  APixel oneZeroLeft = new BorderPixel();
  APixel oneZero = new Pixel(Color.white);
  APixel oneOne = new Pixel(Color.white);
  APixel oneTwo = new Pixel(new Color(142, 207, 242));
  APixel oneThree = new Pixel(new Color(142, 207, 242));
  APixel oneThreeRight = new BorderPixel();

  ArrayList<APixel> row3 = new ArrayList<>();

  APixel twoZeroLeft = new BorderPixel();
  APixel twoZero = new Pixel(Color.white);
  APixel twoOne = new Pixel(Color.white);
  APixel twoTwo = new Pixel(Color.white);
  APixel twoThree = new Pixel(new Color(142, 207, 242));
  APixel twoThreeRight = new BorderPixel();

  ArrayList<APixel> row4 = new ArrayList<>();

  APixel threeZeroLeft = new BorderPixel();
  APixel threeZero = new Pixel(new Color(142, 207, 242));
  APixel threeOne = new Pixel(new Color(142, 207, 242));
  APixel threeTwo = new Pixel(new Color(142, 207, 242));
  APixel threeThree = new Pixel(new Color(142, 207, 242));
  APixel threeThreeRight = new BorderPixel();

  ArrayList<APixel> row5 = new ArrayList<>();

  APixel threeZeroSW = new BorderPixel();
  APixel threeZeroBelow = new BorderPixel();
  APixel threeOneBelow = new BorderPixel();
  APixel threeTwoBelow = new BorderPixel();
  APixel threeThreeBelow = new BorderPixel();
  APixel threeThreeSE = new BorderPixel();

  ArrayList<ArrayList<APixel>> grid = new ArrayList<>();

  void initGraph() {
    this.row0.clear();
    this.row0.add(this.zeroZeroNW);
    this.row0.add(this.zeroZeroAbove);
    this.row0.add(this.zeroOneAbove);
    this.row0.add(this.zeroTwoAbove);
    this.row0.add(this.zeroThreeAbove);
    this.row0.add(this.zeroThreeNE);

    this.row1.clear();
    this.row1.add(this.zeroZeroLeft);
    this.row1.add(this.zeroZero);
    this.row1.add(this.zeroOne);
    this.row1.add(this.zeroTwo);
    this.row1.add(this.zeroThree);
    this.row1.add(this.zeroThreeRight);

    this.row2.clear();
    this.row2.add(this.oneZeroLeft);
    this.row2.add(this.oneZero);
    this.row2.add(this.oneOne);
    this.row2.add(this.oneTwo);
    this.row2.add(this.oneThree);
    this.row2.add(this.oneThreeRight);

    this.row3.clear();
    this.row3.add(this.twoZeroLeft);
    this.row3.add(this.twoZero);
    this.row3.add(this.twoOne);
    this.row3.add(this.twoTwo);
    this.row3.add(this.twoThree);
    this.row3.add(this.twoThreeRight);

    this.row4.clear();
    this.row4.add(this.threeZeroLeft);
    this.row4.add(this.threeZero);
    this.row4.add(this.threeOne);
    this.row4.add(this.threeTwo);
    this.row4.add(this.threeThree);
    this.row4.add(this.threeThreeRight);

    this.row5.clear();
    this.row5.add(this.threeZeroSW);
    this.row5.add(this.threeZeroBelow);
    this.row5.add(this.threeOneBelow);
    this.row5.add(this.threeTwoBelow);
    this.row5.add(this.threeThreeBelow);
    this.row5.add(this.threeThreeSE);

    this.grid.clear();
    this.grid.add(this.row0);
    this.grid.add(this.row1);
    this.grid.add(this.row2);
    this.grid.add(this.row3);
    this.grid.add(this.row4);
    this.grid.add(this.row5);

  }

  // -------------- GRAPH CONSTRUCTION METHODS ----------------//

  // makes a 6 x 6 graph from an AL<AL<APixel>> with 4 x 4 pixels surrounded by
  // borderpixels. graph has bottomleft border pixel as "sentinel"
  boolean testMakeCorrectlyLinkedGraph(Tester t) {

    this.initGraph();

    return
    // calling makeConnections with an AL<AL<APixels>> consisting of a 4 x 4
    // pixelated image surrounded
    // by borderPixels returns a new 6 x 6 Graph with the bottomleft border pixel as
    // the access point
    t.checkExpect(new Utils().makeConnections(this.grid, 4, 4), new Graph(6, 6, zeroZeroNW));
  }

  // wellformed checks
  boolean testGraphWellFormededness(Tester t) {
    this.initGraph();

    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    return t.checkExpect(this.oneZero.west, this.oneZeroLeft)
        // the pixel north of pixel @ (2, 2) is pixel (1, 2)
        && t.checkExpect(this.twoTwo.north, this.oneTwo)
        // the pixel east of a border pixel on the rightmost side is itself
        && t.checkExpect(this.threeThreeRight.east, this.threeThreeRight)
        // the pixel north of pixel @ (2, 3) has pixel (2, 3) to its south
        && t.checkExpect(this.twoThree.north.south, this.twoThree)
        // the pixel south of the pixel south of pixel @ (1, 2) is pixel (3, 2)
        && t.checkExpect(this.oneTwo.south.south, this.threeTwo)
        // the pixel to the northeast of pixel @ (3, 1) is pixel (2, 2)
        && t.checkExpect(this.threeOne.north.east, this.twoTwo)
        // the pixel to the west of a borderpixel @(2,2 on the leftmost side is itself
        && t.checkExpect(this.twoZeroLeft.west, twoZeroLeft)
        // the pixel to the north of a borderpixel on the top side is itself
        && t.checkExpect(this.zeroTwoAbove.north, zeroTwoAbove)
        // the pixel to the southeast of produced graph's sentinel pixel is pixel (0, 0)
        && t.checkExpect(g.topLeft.south.east, this.zeroZero);
  }

  // -------------- APIXEL METHODS --------------//

  boolean testCalcBrightness(Tester t) {
    APixel mauve = new Pixel(new Color(233, 168, 240));
    APixel border = new BorderPixel();
    APixel babyBlue = new Pixel(new Color(168, 207, 240));
    APixel grey = new Pixel(new Color(87, 87, 87));

    // lighter colors (mauve, babyBlue) have brightness closer to 1
    return t.checkExpect(mauve.calcBrightness(), 0.8352941176470589)
        && t.checkExpect(babyBlue.calcBrightness(), 0.803921568627451)
        // darker colors (borderPixels, dark grey) have brightness closer to 0
        && t.checkExpect(border.calcBrightness(), 0.0)
        && t.checkExpect(grey.calcBrightness(), 0.3411764705882353);
  }

  boolean testPixelRemoveFromSouthWest(Tester t) {
    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    eNW.removeVerticalSeam(gNW);

    // the pixel to the west of eNW should have a mutual connection with eNW's south
    // field
    return t.checkExpect(dNW.south, hNW) && t.checkExpect(hNW.north, dNW)
        // the pixel to the east of eNW should have a mutual connection with eNW's west field
        && t.checkExpect(fNW.west, dNW) && t.checkExpect(dNW.east, fNW);
  }

  boolean testPixelRemoveFromSouth(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    bNW.removeVerticalSeam(eNW);

    // so bNW has correctly only removed its horizontal connections
    return t.checkExpect(cNW.west, aNW) && t.checkExpect(aNW.east, cNW);
           
  }

  boolean testPixelRemoveFromSouthEast(Tester t) {
    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    aNW.removeVerticalSeam(eNW);

    return
    // the pixel to the east of aNW should have a mutual connection with aNW's south
    // field
    t.checkExpect(bNW.south, dNW) && t.checkExpect(dNW.north, bNW)
        // the pixel to the east of aNW should have a mutual connection with aNW's west
        // field
        && t.checkExpect(aNW.west.east, bNW) && t.checkExpect(bNW.west, aNW.east.west);
  }

  boolean testPixelRemoveFromEastNorth(Tester t) {
    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    eNW.removeHorizontalSeam(cNW);

    // the pixel south of eNW should have a mutual connection with eNW's south field
    return t.checkExpect(bNW.south, hNW) && t.checkExpect(hNW.north, bNW)
        // the pixel to the east of eNW should have a mutual connection with eNW's west
        // field
        && t.checkExpect(fNW.west, bNW) && t.checkExpect(bNW.east, fNW);
  }

  boolean testPixelRemoveFromEast(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    dNW.removeHorizontalSeam(eNW);

    return t.checkExpect(aNW.south, gNW) && t.checkExpect(gNW.north, aNW);
    // so dNW has correctly only removed its vertical connections

  }

  boolean testPixelRemoveFromEastSouth(Tester t) {
    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    eNW.removeHorizontalSeam(iNW);

    return
    // the pixel north of eNW should have a mutual connection with eNW's south
    // field
    t.checkExpect(bNW.south, hNW) && t.checkExpect(hNW.north, bNW)
        // the pixel to the east of eNW should have a mutual connection with eNW's
        // southern
        // field
        && t.checkExpect(fNW.west, hNW) && t.checkExpect(hNW.east, fNW);
  }

  // ------------ SEAMFINDING IN GRAPH -----------------//

  void connectPixels3x3(APixel a, APixel b, APixel c, APixel d, APixel e, APixel f, APixel g,
      APixel h, APixel i) {

    BorderPixel b1 = new BorderPixel();
    BorderPixel b2 = new BorderPixel();
    BorderPixel b3 = new BorderPixel();
    BorderPixel b4 = new BorderPixel();
    BorderPixel b5 = new BorderPixel();
    BorderPixel b6 = new BorderPixel();
    BorderPixel b7 = new BorderPixel();
    BorderPixel b8 = new BorderPixel();
    BorderPixel b9 = new BorderPixel();
    BorderPixel b10 = new BorderPixel();
    BorderPixel b11 = new BorderPixel();
    BorderPixel b12 = new BorderPixel();
    BorderPixel b13 = new BorderPixel();
    BorderPixel b14 = new BorderPixel();
    BorderPixel b15 = new BorderPixel();
    BorderPixel b16 = new BorderPixel();

    a.north = b1;
    a.west = b15;
    a.east = b;
    a.south = d;

    b.north = b2;
    b.west = a;
    b.east = c;
    b.south = e;

    c.north = b3;
    c.west = b;
    c.east = b5;
    c.south = f;

    d.north = a;
    d.west = b14;
    d.east = e;
    d.south = g;

    e.north = b;
    e.west = d;
    e.east = f;
    e.south = h;

    f.north = c;
    f.west = e;
    f.east = b6;
    f.south = i;

    g.north = d;
    g.west = b13;
    g.east = h;
    g.south = b11;

    h.north = e;
    h.west = g;
    h.east = i;
    h.south = b10;

    i.north = f;
    i.west = h;
    i.east = b7;
    i.south = b9;

    b1.north = b1;
    b1.west = b16;
    b1.east = b2;
    b1.south = a;

    b2.north = b2;
    b2.west = b1;
    b2.east = b3;
    b2.south = b;

    b3.north = b3;
    b3.west = b2;
    b3.east = b4;
    b3.south = c;

    b4.north = b4;
    b4.west = b3;
    b4.east = b4;
    b4.south = b5;

    b5.north = b4;
    b5.west = c;
    b5.east = b5;
    b5.south = b6;

    b6.north = b5;
    b6.west = f;
    b6.east = b6;
    b6.south = b7;

    b7.north = b6;
    b7.west = i;
    b7.east = b7;
    b7.south = b8;

    b8.north = b7;
    b8.west = b9;
    b8.east = b8;
    b8.south = b8;

    b9.north = i;
    b9.west = b10;
    b9.east = b8;
    b9.south = b9;

    b10.north = h;
    b10.west = b11;
    b10.east = b9;
    b10.south = b10;

    b11.north = g;
    b11.west = b12;
    b11.east = b10;
    b11.south = b11;

    b12.north = b13;
    b12.west = b12;
    b12.east = b11;
    b12.south = b12;

    b13.north = b14;
    b13.west = b13;
    b13.east = g;
    b13.south = b12;

    b14.north = b15;
    b14.west = b14;
    b14.east = d;
    b14.south = b13;

    b15.north = b16;
    b15.west = b15;
    b15.east = a;
    b15.south = b14;

    b16.north = b16;
    b16.west = b16;
    b16.east = b1;
    b16.south = b15;
  }

  boolean testCopySeam(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);
    SeamInfo cpyVert = new Utils().makeSeamInfoCopy(g.findVerticalSeam());
    SeamInfo cpyHoriz = new Utils().makeSeamInfoCopy(g.findHorizontalSeam());

    return t.checkExpect(g.findVerticalSeam(), cpyVert)
        && t.checkExpect(g.findHorizontalSeam(), cpyHoriz);
  }

  boolean testFindVerticalSeamNW(Tester t) {

    SeamCarverWorld make3x3NW = new SeamCarverWorld(new FromFileImage("3x3NW.png"));

    Graph graph3x3NW = make3x3NW.pixels;

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    SeamInfo seamPt1 = new SeamInfo(aNW);
    SeamInfo seamPt2 = new SeamInfo(eNW, eNW.calcEnergy() + seamPt1.totalWeight, seamPt1);
    SeamInfo seamPt3 = new SeamInfo(gNW, gNW.calcEnergy() + seamPt2.totalWeight, seamPt2);

    return t.checkExpect(graph3x3NW.findVerticalSeam(), seamPt3);
  }

  boolean testFindVerticalSeamN(Tester t) {

    SeamCarverWorld make3x3N = new SeamCarverWorld(new FromFileImage("3x3N.png"));

    Graph graph3x3N = make3x3N.pixels;

    APixel aN = new Pixel(new Color(47, 54, 153));
    APixel bN = new Pixel(new Color(255, 249, 189));
    APixel cN = new Pixel(new Color(47, 54, 153));
    APixel dN = new Pixel(new Color(47, 54, 153));
    APixel eN = new Pixel(new Color(255, 249, 189));
    APixel fN = new Pixel(new Color(47, 54, 153));
    APixel gN = new Pixel(new Color(47, 54, 153));
    APixel hN = new Pixel(new Color(255, 249, 189));
    APixel iN = new Pixel(new Color(47, 54, 153));

    connectPixels3x3(aN, bN, cN, dN, eN, fN, gN, hN, iN);

    SeamInfo seamPt1 = new SeamInfo(bN);
    SeamInfo seamPt2 = new SeamInfo(eN, eN.calcEnergy() + seamPt1.totalWeight, seamPt1);
    SeamInfo seamPt3 = new SeamInfo(hN, hN.calcEnergy() + seamPt2.totalWeight, seamPt2);

    return t.checkExpect(graph3x3N.findVerticalSeam(), seamPt3);
  }

  boolean testFindVerticalSeamNE(Tester t) {

    SeamCarverWorld make3x3NE = new SeamCarverWorld(new FromFileImage("3x3NE.png"));

    Graph graph3x3NE = make3x3NE.pixels;

    APixel aNE = new Pixel(new Color(47, 54, 153));
    APixel bNE = new Pixel(new Color(47, 54, 153));
    APixel cNE = new Pixel(new Color(255, 249, 189));
    APixel dNE = new Pixel(new Color(47, 54, 153));
    APixel eNE = new Pixel(new Color(255, 249, 189));
    APixel fNE = new Pixel(new Color(47, 54, 153));
    APixel gNE = new Pixel(new Color(255, 249, 189));
    APixel hNE = new Pixel(new Color(47, 54, 153));
    APixel iNE = new Pixel(new Color(47, 54, 153));

    connectPixels3x3(aNE, bNE, cNE, dNE, eNE, fNE, gNE, hNE, iNE);

    SeamInfo seamPt1 = new SeamInfo(aNE);
    SeamInfo seamPt2 = new SeamInfo(eNE, eNE.calcEnergy() + seamPt1.totalWeight, seamPt1);
    SeamInfo seamPt3 = new SeamInfo(gNE, gNE.calcEnergy() + seamPt2.totalWeight, seamPt2);

    return t.checkExpect(graph3x3NE.findVerticalSeam(), seamPt3);
  }

  // tests minSeam method in Utils
  boolean testMinSeam(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    ArrayList<SeamInfo> initRow = new ArrayList<>();
    APixel curr = g.topLeft.south.east;

    for (int i = 0; i < g.width - 2; i += 1) {
      initRow.add(new SeamInfo(curr));
      curr = curr.east;
    }

    ArrayList<SeamInfo> seams = new ArrayList<SeamInfo>();
    ArrayList<SeamInfo> seams2 = new ArrayList<SeamInfo>();
    seams = g.topLeft.south.south.east.findVerticalSeamRow(initRow);
    seams2.addAll(seams);
    seams2.remove(3);
    // the first minimum seam begins bottom row's 4th pixel
    return t.checkExpect(new Utils().findMin(seams), seams.get(3))
        // the second minimum seam after the first minimum seam is begins at the bottom
        // row's 3rd pixel
        && t.checkExpect(new Utils().findMin(seams2), seams2.get(2));

  }

  // ensures that minimum Seam from Graph consists of the correct total weights
  boolean testFindVerticalSeamWeight(Tester t) {
    this.initGraph();
    Graph fourByFour = new Utils().makeConnections(this.grid, 4, 4);
    SeamInfo s = fourByFour.findVerticalSeam();

    return t.checkExpect(s.totalWeight, 8.561216005824793)
        && t.checkExpect(s.cameFrom.totalWeight, 4.961903841196798)
        && t.checkExpect(s.cameFrom.cameFrom.totalWeight, 4.242640687119285)
        && t.checkExpect(s.cameFrom.cameFrom.cameFrom.totalWeight, 3.27764790338235);
  }

  // ensures that minimum Seam from Graph consists of the correct total weights
  boolean testFindHorizontalSeamWeight(Tester t) {
    this.initGraph();
    Graph fourByFour = new Utils().makeConnections(this.grid, 4, 4);
    SeamInfo s = fourByFour.findHorizontalSeam();

    return t.checkExpect(s.totalWeight, 8.597723084088056)
        && t.checkExpect(s.cameFrom.totalWeight, 5.272288382368329)
        && t.checkExpect(s.cameFrom.cameFrom.totalWeight, 4.5530252282908155)
        && t.checkExpect(s.cameFrom.cameFrom.cameFrom.totalWeight, 3.833762074213302);
  }

  // ----------- HORIZTONAL SEAM FINDING IN PIXEL ---------------- \\

  boolean testFindHorizontalSeamNW(Tester t) {

    SeamCarverWorld make3x3NW = new SeamCarverWorld(new FromFileImage("3x3NW.png"));

    Graph graph3x3NW = make3x3NW.pixels;

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    SeamInfo seamPt1 = new SeamInfo(aNW);
    SeamInfo seamPt2 = new SeamInfo(eNW, eNW.calcEnergy() + seamPt1.totalWeight, seamPt1);
    SeamInfo seamPt3 = new SeamInfo(cNW, cNW.calcEnergy() + seamPt2.totalWeight, seamPt2);

    return t.checkExpect(graph3x3NW.findHorizontalSeam(), seamPt3);
  }

  boolean testFindHorizontalSeamN(Tester t) {

    SeamCarverWorld make3x3N = new SeamCarverWorld(new FromFileImage("3x3N.png"));

    Graph graph3x3N = make3x3N.pixels;

    APixel aN = new Pixel(new Color(47, 54, 153));
    APixel bN = new Pixel(new Color(255, 249, 189));
    APixel cN = new Pixel(new Color(47, 54, 153));
    APixel dN = new Pixel(new Color(47, 54, 153));
    APixel eN = new Pixel(new Color(255, 249, 189));
    APixel fN = new Pixel(new Color(47, 54, 153));
    APixel gN = new Pixel(new Color(47, 54, 153));
    APixel hN = new Pixel(new Color(255, 249, 189));
    APixel iN = new Pixel(new Color(47, 54, 153));

    connectPixels3x3(aN, bN, cN, dN, eN, fN, gN, hN, iN);

    SeamInfo seamPt1 = new SeamInfo(aN);
    SeamInfo seamPt2 = new SeamInfo(eN, eN.calcEnergy() + seamPt1.totalWeight, seamPt1);
    SeamInfo seamPt3 = new SeamInfo(cN, cN.calcEnergy() + seamPt2.totalWeight, seamPt2);

    return t.checkExpect(graph3x3N.findHorizontalSeam(), seamPt3);
  }

  boolean testFindHorizontalSeamNE(Tester t) {

    SeamCarverWorld make3x3NE = new SeamCarverWorld(new FromFileImage("3x3NE.png"));

    Graph graph3x3NE = make3x3NE.pixels;

    APixel aNE = new Pixel(new Color(47, 54, 153));
    APixel bNE = new Pixel(new Color(47, 54, 153));
    APixel cNE = new Pixel(new Color(255, 249, 189));
    APixel dNE = new Pixel(new Color(47, 54, 153));
    APixel eNE = new Pixel(new Color(255, 249, 189));
    APixel fNE = new Pixel(new Color(47, 54, 153));
    APixel gNE = new Pixel(new Color(255, 249, 189));
    APixel hNE = new Pixel(new Color(47, 54, 153));
    APixel iNE = new Pixel(new Color(47, 54, 153));

    connectPixels3x3(aNE, bNE, cNE, dNE, eNE, fNE, gNE, hNE, iNE);

    SeamInfo seamPt1 = new SeamInfo(aNE);
    SeamInfo seamPt2 = new SeamInfo(eNE, eNE.calcEnergy() + seamPt1.totalWeight, seamPt1);
    SeamInfo seamPt3 = new SeamInfo(cNE, cNE.calcEnergy() + seamPt2.totalWeight, seamPt2);

    return t.checkExpect(graph3x3NE.findHorizontalSeam(), seamPt3);
  }

  // --------- FINDING SEAMS IN PIXEL --------- //

  boolean testFindVerticalSeamThreeNeighbors(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    ArrayList<SeamInfo> seamInfoAcc = new ArrayList<SeamInfo>();
    ArrayList<SeamInfo> possibleSeams = new ArrayList<SeamInfo>();

    SeamInfo seamA = new SeamInfo(aNW);
    SeamInfo seamB = new SeamInfo(bNW);
    SeamInfo seamC = new SeamInfo(cNW);

    seamInfoAcc.add(seamA);
    seamInfoAcc.add(seamB);
    seamInfoAcc.add(seamC);

    SeamInfo seam1 = new SeamInfo(dNW, dNW.calcEnergy() + seamA.totalWeight, seamA);

    possibleSeams.add(seam1);

    SeamInfo seam2 = new SeamInfo(eNW, eNW.calcEnergy() + seamA.totalWeight, seamA);
    SeamInfo seam3 = new SeamInfo(fNW, fNW.calcEnergy() + seamC.totalWeight, seamC);

    ArrayList<SeamInfo> result = new ArrayList<SeamInfo>();
    result.add(seam1);
    result.add(seam2);
    result.add(seam3);

    return t.checkExpect(eNW.findVerticalSeamThreeNeighbors(seamInfoAcc, possibleSeams), result)
        // checks border pixel case
        && t.checkExpect(aNW.west.findVerticalSeamThreeNeighbors(seamInfoAcc, possibleSeams),
            possibleSeams);
  }

  boolean testFindVerticalSeamTwoNeighbors(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    ArrayList<SeamInfo> seamInfoAcc = new ArrayList<SeamInfo>();
    ArrayList<SeamInfo> possibleSeams = new ArrayList<SeamInfo>();

    SeamInfo seamA = new SeamInfo(aNW);
    SeamInfo seamB = new SeamInfo(bNW);
    SeamInfo seamC = new SeamInfo(cNW);

    seamInfoAcc.add(seamB);
    seamInfoAcc.add(seamC);

    SeamInfo seam1 = new SeamInfo(dNW, dNW.calcEnergy() + seamA.totalWeight, seamA);
    SeamInfo seam2 = new SeamInfo(eNW, eNW.calcEnergy() + seamA.totalWeight, seamA);

    possibleSeams.add(seam1);
    possibleSeams.add(seam2);

    SeamInfo seam3 = new SeamInfo(fNW, fNW.calcEnergy() + seamC.totalWeight, seamC);

    ArrayList<SeamInfo> result = new ArrayList<SeamInfo>();
    result.add(seam1);
    result.add(seam2);
    result.add(seam3);

    return t.checkExpect(fNW.findSeamTwoNeighbors(seamInfoAcc, possibleSeams), result)
        // checks border pixel case
        && t.checkExpect(fNW.west.findSeamTwoNeighbors(seamInfoAcc, possibleSeams), possibleSeams);
  }

  boolean testFindVerticalSeamRow(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    ArrayList<SeamInfo> seamInfoAcc = new ArrayList<SeamInfo>();

    SeamInfo seamA = new SeamInfo(aNW);
    SeamInfo seamB = new SeamInfo(bNW);
    SeamInfo seamC = new SeamInfo(cNW);

    seamInfoAcc.add(seamA);
    seamInfoAcc.add(seamB);
    seamInfoAcc.add(seamC);

    SeamInfo seamE = new SeamInfo(eNW, eNW.calcEnergy() + seamA.totalWeight, seamA);

    ArrayList<SeamInfo> result = new ArrayList<SeamInfo>();

    SeamInfo seamG = new SeamInfo(gNW, gNW.calcEnergy() + seamE.totalWeight, seamE);
    SeamInfo seamH = new SeamInfo(hNW, hNW.calcEnergy() + seamE.totalWeight, seamE);
    SeamInfo seamI = new SeamInfo(iNW, iNW.calcEnergy() + seamE.totalWeight, seamE);

    result.add(seamG);
    result.add(seamH);
    result.add(seamI);

    return t.checkExpect(dNW.findVerticalSeamRow(seamInfoAcc), result);
  }

  boolean testFindHorizontalSeamThreeNeighbors(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    ArrayList<SeamInfo> seamInfoAcc = new ArrayList<SeamInfo>();
    ArrayList<SeamInfo> possibleSeams = new ArrayList<SeamInfo>();

    SeamInfo seamA = new SeamInfo(aNW);
    SeamInfo seamD = new SeamInfo(dNW);
    SeamInfo seamG = new SeamInfo(gNW);

    seamInfoAcc.add(seamA);
    seamInfoAcc.add(seamD);
    seamInfoAcc.add(seamG);

    SeamInfo seam1 = new SeamInfo(bNW, bNW.calcEnergy() + seamA.totalWeight, seamA);

    possibleSeams.add(seam1);

    SeamInfo seam2 = new SeamInfo(eNW, eNW.calcEnergy() + seamA.totalWeight, seamA);
    SeamInfo seam3 = new SeamInfo(hNW, hNW.calcEnergy() + seamG.totalWeight, seamG);

    ArrayList<SeamInfo> result = new ArrayList<SeamInfo>();
    result.add(seam1);
    result.add(seam2);
    result.add(seam3);

    return t.checkExpect(eNW.findHorizontalSeamThreeNeighbors(seamInfoAcc, possibleSeams), result)
        // checks border pixel case
        && t.checkExpect(gNW.south.findHorizontalSeamThreeNeighbors(seamInfoAcc, possibleSeams),
            possibleSeams);
  }

  boolean testFindHorizontalSeamTwoNeighbors(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    ArrayList<SeamInfo> seamInfoAcc = new ArrayList<SeamInfo>();
    ArrayList<SeamInfo> possibleSeams = new ArrayList<SeamInfo>();

    SeamInfo seamA = new SeamInfo(aNW);
    SeamInfo seamD = new SeamInfo(dNW);
    SeamInfo seamG = new SeamInfo(gNW);

    seamInfoAcc.add(seamD);
    seamInfoAcc.add(seamG);

    SeamInfo seam1 = new SeamInfo(bNW, bNW.calcEnergy() + seamA.totalWeight, seamA);
    SeamInfo seam2 = new SeamInfo(eNW, eNW.calcEnergy() + seamA.totalWeight, seamA);

    possibleSeams.add(seam1);
    possibleSeams.add(seam2);

    SeamInfo seam3 = new SeamInfo(hNW, hNW.calcEnergy() + seamG.totalWeight, seamG);

    ArrayList<SeamInfo> result = new ArrayList<SeamInfo>();
    result.add(seam1);
    result.add(seam2);
    result.add(seam3);

    return t.checkExpect(hNW.findSeamTwoNeighbors(seamInfoAcc, possibleSeams), result)
        // checks border pixel case
        && t.checkExpect(hNW.south.findSeamTwoNeighbors(seamInfoAcc, possibleSeams), possibleSeams);
  }

  boolean testFindHorizontalSeamCol(Tester t) {

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    ArrayList<SeamInfo> seamInfoAcc = new ArrayList<SeamInfo>();

    SeamInfo seamA = new SeamInfo(aNW);
    SeamInfo seamD = new SeamInfo(dNW);
    SeamInfo seamG = new SeamInfo(gNW);

    seamInfoAcc.add(seamA);
    seamInfoAcc.add(seamD);
    seamInfoAcc.add(seamG);

    SeamInfo seamE = new SeamInfo(eNW, eNW.calcEnergy() + seamA.totalWeight, seamA);

    ArrayList<SeamInfo> result = new ArrayList<SeamInfo>();

    SeamInfo seamC = new SeamInfo(cNW, cNW.calcEnergy() + seamE.totalWeight, seamE);
    SeamInfo seamF = new SeamInfo(fNW, fNW.calcEnergy() + seamE.totalWeight, seamE);
    SeamInfo seamI = new SeamInfo(iNW, iNW.calcEnergy() + seamE.totalWeight, seamE);

    result.add(seamC);
    result.add(seamF);
    result.add(seamI);

    return t.checkExpect(bNW.findHorizontalSeamCol(seamInfoAcc), result);
  }
  // ------------ SHOULD END IN GRAPH -----------//

  boolean testShouldEndCondition(Tester t) {

    SeamCarverWorld make3x3NE = new SeamCarverWorld(new FromFileImage("3x3NE.png"));
    Graph graph3x3NE = make3x3NE.pixels;

    SeamCarverWorld make3x2 = new SeamCarverWorld(new FromFileImage("2x3live.png"));
    Graph graph3x2 = make3x2.pixels;

    SeamCarverWorld make3x1 = new SeamCarverWorld(new FromFileImage("1x3live.png"));
    Graph graph3x1 = make3x1.pixels;

    SeamCarverWorld make4x3 = new SeamCarverWorld(new FromFileImage("4x3.png"));
    Graph graph4x3 = make4x3.pixels;

    SeamCarverWorld make4x2 = new SeamCarverWorld(new FromFileImage("4x2.png"));
    Graph graph4x2 = make4x2.pixels;

    SeamCarverWorld make4x1 = new SeamCarverWorld(new FromFileImage("4x1.png"));
    Graph graph4x1 = make4x1.pixels;

    return t.checkExpect(graph3x3NE.shouldEnd(), false)
        && t.checkExpect(graph3x2.shouldEnd(), false) && t.checkExpect(graph3x1.shouldEnd(), true)
        && t.checkExpect(graph4x3.shouldEnd(), false) && t.checkExpect(graph4x2.shouldEnd(), false)
        && t.checkExpect(graph4x1.shouldEnd(), true);
  }

  // -------------- SEAM REMOVAL -------------- //

  boolean testConnectivityAfterRemovingOneSeam(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);
    // get min
    SeamInfo min = g.findVerticalSeam();
    // rip seam
    g.ripSeamVertical(min);

    SeamCarverWorld s = new SeamCarverWorld(new FromFileImage("3x4.png"));
    // makes a well connected graph, as proved in tests checking well-formed in
    // initial graph's construction
    Graph afterOneRip = s.pixels;
    return t.checkExpect(g.topLeft, afterOneRip.topLeft);
  }

  boolean testConnectivityAfterRemovingTwoSeams(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);
    // get min
    SeamInfo min = g.findVerticalSeam();
    // rip seam
    g.ripSeamVertical(min);

    // get 2nd min
    SeamInfo min2 = g.findVerticalSeam();
    // rip 2nd seam
    g.ripSeamVertical(min2);

    SeamCarverWorld s = new SeamCarverWorld(new FromFileImage("2x4.png"));
    // makes a well connected graph, as proved in tests checking well-formed in
    // initial graph's construction
    Graph afterTwoRips = s.pixels;
    return t.checkExpect(g.topLeft, afterTwoRips.topLeft);
  }

  boolean testRemoveMinimumSeamOnceImage(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);
    // get min
    SeamInfo min = g.findVerticalSeam();
    // rip seam

    g.ripSeamVertical(min);

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, Color.white);
    afterOneSeam.setColorAt(1, 2, Color.white);
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 3, new Color(142, 207, 242));
    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }

  boolean testRemoveMinimumSeamTwiceImage(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findVerticalSeam();
    // rip seam first time
    g.ripSeamVertical(min);
    // get min 2nd time
    SeamInfo min2 = g.findVerticalSeam();
    // rip seam 2nd time
    g.ripSeamVertical(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, Color.white);
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 2, Color.white);
    afterTwoSeams.setColorAt(1, 2, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 3, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 3, new Color(142, 207, 242));

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterTwoSeams);
  }

  boolean testRemoveMinimumSeamOnceRipHelp(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findVerticalSeam();
    // rip seam

    APixel curr = min.pixel;
    curr.south.fixVerticalBorder();

    curr.west.east = curr.east;
    curr.east.west = curr.west;

    g.ripSeamVerticalHelp(min);

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, Color.white);
    afterOneSeam.setColorAt(1, 2, Color.white);
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 3, new Color(142, 207, 242));
    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }

  boolean testRemoveMinimumSeamTwiceRipHelp(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findVerticalSeam();

    APixel curr = min.pixel;
    curr.south.fixVerticalBorder();

    curr.west.east = curr.east;
    curr.east.west = curr.west;

    g.ripSeamVerticalHelp(min);

    // get min 2nd time
    SeamInfo min2 = g.findVerticalSeam();

    APixel curr2 = min2.pixel;
    curr2.south.fixVerticalBorder();

    curr2.west.east = curr2.east;
    curr2.east.west = curr2.west;

    g.ripSeamVerticalHelp(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, Color.white);
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 2, Color.white);
    afterTwoSeams.setColorAt(1, 2, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 3, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 3, new Color(142, 207, 242));

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterTwoSeams);
  }

  // --------------- SEAM RIPPING HORIZTONAL ------------------ \\

  boolean testConnectivityAfterRemovingOneSeamHorizontal(Tester t) {

    this.initGraph();

    Graph g = new Utils().makeConnections(this.grid, 4, 4);
    // get min
    SeamInfo min = g.findHorizontalSeam();

    // rip seam
    g.ripSeamHorizontal(min);

    SeamCarverWorld s = new SeamCarverWorld(new FromFileImage("4x3.png"));
    // makes a well connected graph, as proved in tests checking well-formed in
    // initial graph's construction
    Graph afterOneRip = s.pixels;

    return t.checkExpect(g.topLeft, afterOneRip.topLeft);
  }

  boolean testConnectivityAfterRemovingTwoSeamsHorizontal(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);
    // get min
    SeamInfo min = g.findHorizontalSeam();
    // rip seam
    g.ripSeamHorizontal(min);

    // get 2nd min
    SeamInfo min2 = g.findHorizontalSeam();
    // rip 2nd seam
    g.ripSeamHorizontal(min2);

    SeamCarverWorld s = new SeamCarverWorld(new FromFileImage("4x2.png"));
    // makes a well connected graph, as proved in tests checking well-formed in
    // initial graph's construction
    Graph afterTwoRips = s.pixels;
    return t.checkExpect(g.topLeft, afterTwoRips.topLeft);
  }

  boolean testRemoveMinimumSeamOnceImageHorizontal(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findHorizontalSeam();
    // rip seam

    // rip seam first time
    g.ripSeamHorizontal(min);

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 2, new Color(142, 207, 242));

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }

  boolean testRemoveMinimumSeamTwiceImageHorizontal(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findHorizontalSeam();
    // rip seam first time
    g.ripSeamHorizontal(min);
    // get min 2nd time
    SeamInfo min2 = g.findHorizontalSeam();
    // rip seam 2nd time
    g.ripSeamHorizontal(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 1, new Color(142, 207, 242));

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterTwoSeams);
  }

  boolean testRemoveMinimumSeamOnceRipHelpHorizontal(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findHorizontalSeam();
    // rip seam

    APixel curr = min.pixel;
    curr.east.fixHorizontalBorder();

    curr.north.south = curr.south;
    curr.south.north = curr.north;

    g.ripSeamHorizontalHelp(min);

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 2, new Color(142, 207, 242));
    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }

  boolean testRemoveMinimumSeamTwiceRipHelpHorizontal(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findHorizontalSeam();

    APixel curr = min.pixel;
    curr.east.fixHorizontalBorder();

    curr.north.south = curr.south;
    curr.south.north = curr.north;

    g.ripSeamHorizontalHelp(min);

    // get min 2nd time
    SeamInfo min2 = g.findHorizontalSeam();

    APixel curr2 = min2.pixel;
    curr2.east.fixHorizontalBorder();

    curr2.north.south = curr2.south;
    curr2.south.north = curr2.north;

    g.ripSeamHorizontalHelp(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 1, new Color(142, 207, 242));

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterTwoSeams);
  }

  // ------------- RENDERING ------------ //

  ComputedPixelImage makeCPI(FromFileImage fileImage, int width, int height) {

    ComputedPixelImage pixelatedImage = new ComputedPixelImage(width, height);
    // converts file image to pixelatedImage
    for (int i = 0; i < height; i += 1) {
      for (int j = 0; j < width; j += 1) {
        Color color = fileImage.getColorAt(j, i);
        pixelatedImage.setColorAt(j, i, color);
      }
    }

    return pixelatedImage;
  }

  ComputedPixelImage makeCPIGray(APixel start, int width, int height, double maxEnergy) {

    ComputedPixelImage pixelatedImage = new ComputedPixelImage(width, height);
    APixel curr = start;
    APixel currRowStart = start;
    for (int i = 0; i < height; i += 1) {
      for (int j = 0; j < width; j += 1) {
        float gray = (float) (curr.calcEnergy() / maxEnergy);
        pixelatedImage.setColorAt(j, i, new Color(gray, gray, gray));
        curr = curr.east;
      }
      currRowStart = currRowStart.south;
      curr = currRowStart;
    }

    return pixelatedImage;
  }

  boolean testDrawImage(Tester t) {
    ComputedPixelImage image = new ComputedPixelImage(3, 3);
    ComputedPixelImage result = makeCPI(new FromFileImage("3x3NW.png"), 3, 3);

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    aNW.drawPixelImage(image, 0, 0, 3, 3);

    return t.checkExpect(image, result);
  }

  boolean testDrawPixelRow(Tester t) {

    ComputedPixelImage image = new ComputedPixelImage(3, 3);
    ComputedPixelImage result = new ComputedPixelImage(3, 3);

    result.setColorAt(0, 0, (new Color(255, 249, 189)));
    result.setColorAt(1, 0, (new Color(47, 54, 153)));
    result.setColorAt(2, 0, (new Color(47, 54, 153)));

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    aNW.drawPixelRow(image, 0, 0, 3, 3);

    return t.checkExpect(image, result);
  }

  // test method to find the max energy from a graph, needed to convert to
  // grayscale
  boolean testfindMaxEnergy(Tester t) {
    initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    Graph nw = new Graph(4, 4, aNW.north.west);

    return t.checkExpect(g.findMaxEnergy(), 4.0)
        && t.checkExpect(nw.findMaxEnergy(), 2.7263674706053633);
  }

  boolean testDrawPixelImageGray(Tester t) {
    ComputedPixelImage image = new ComputedPixelImage(3, 3);

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    ComputedPixelImage result = makeCPIGray(aNW, 3, 3, 5.66);

    aNW.drawPixelImageGray(image, 0, 0, 3, 3, 5.66);

    return t.checkExpect(image, result);
  }

  boolean testDrawPixelRowGray(Tester t) {
    ComputedPixelImage image = new ComputedPixelImage(3, 3);
    ComputedPixelImage result = new ComputedPixelImage(3, 3);

    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    result.setColorAt(0, 0, (new Color((float) (aNW.calcEnergy() / 5.66),
        (float) (aNW.calcEnergy() / 5.66), (float) (aNW.calcEnergy() / 5.66))));
    result.setColorAt(1, 0, (new Color((float) (bNW.calcEnergy() / 5.66),
        (float) (bNW.calcEnergy() / 5.66), (float) (bNW.calcEnergy() / 5.66))));
    result.setColorAt(2, 0, (new Color((float) (cNW.calcEnergy() / 5.66),
        (float) (cNW.calcEnergy() / 5.66), (float) (cNW.calcEnergy() / 5.66))));

    aNW.drawPixelRowGray(image, 0, 0, 3, 3, 5.66);

    return t.checkExpect(image, result);
  }

  boolean testColorSeam(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    SeamInfo minHoriz = g.findHorizontalSeam();

    SeamInfo minHorizCopy = new Utils().makeSeamInfoCopy(minHoriz);

    g.colorSeam(minHorizCopy);

    SeamInfo minVert = g.findVerticalSeam();

    SeamInfo minVertCopy = new Utils().makeSeamInfoCopy(minVert);

    g.colorSeam(minVertCopy);

    return t.checkExpect(minHorizCopy.pixel.color, Color.red)
        && t.checkExpect(minHorizCopy.cameFrom.pixel.color, Color.red)
        && t.checkExpect(minHorizCopy.cameFrom.cameFrom.pixel.color, Color.red)
        && t.checkExpect(minHorizCopy.cameFrom.cameFrom.cameFrom.pixel.color, Color.red)
        && t.checkExpect(minVertCopy.pixel.color, Color.red)
        && t.checkExpect(minVertCopy.cameFrom.pixel.color, Color.red)
        && t.checkExpect(minVertCopy.cameFrom.cameFrom.pixel.color, Color.red)
        && t.checkExpect(minVertCopy.cameFrom.cameFrom.cameFrom.pixel.color, Color.red);
  }
  // ---------------- REINSERT ----------------- //

  boolean testReinsertPixel(Tester t) {
    APixel aNW = new Pixel(new Color(255, 249, 189));
    APixel bNW = new Pixel(new Color(47, 54, 153));
    APixel cNW = new Pixel(new Color(47, 54, 153));
    APixel dNW = new Pixel(new Color(47, 54, 153));
    APixel eNW = new Pixel(new Color(255, 249, 189));
    APixel fNW = new Pixel(new Color(47, 54, 153));
    APixel gNW = new Pixel(new Color(47, 54, 153));
    APixel hNW = new Pixel(new Color(47, 54, 153));
    APixel iNW = new Pixel(new Color(255, 249, 189));

    connectPixels3x3(aNW, bNW, cNW, dNW, eNW, fNW, gNW, hNW, iNW);

    aNW.reinsert();
    // ensure connections
    return t.checkExpect(aNW.north.south, aNW) && t.checkExpect(aNW.east.west, aNW)
        && t.checkExpect(aNW.south.north, aNW) && t.checkExpect(aNW.west.east, aNW);

  }

  boolean testRemoveConnectivity(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    this.initGraph();
    Graph f = new Utils().makeConnections(this.grid, 4, 4);

    this.initGraph();
    Graph h = new Utils().makeConnections(this.grid, 4, 4);

    this.initGraph();
    Graph d = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findHorizontalSeam();
    // rip seam first time
    g.ripSeamHorizontal(min);
    // get min 2nd time
    SeamInfo min2 = g.findHorizontalSeam();
    // rip seam 2nd time
    g.ripSeamHorizontal(min2);

    // reinsert the last two removed seams and checking connectivity
    g.reinsert();
    g.reinsert();

    h.ripSeamHorizontal(h.findHorizontalSeam());
    h.ripSeamHorizontal(h.findHorizontalSeam());
    h.reinsert();

    d.ripSeamHorizontal(d.findHorizontalSeam());

    this.initGraph();
    Graph a = new Utils().makeConnections(this.grid, 4, 4);

    this.initGraph();
    Graph b = new Utils().makeConnections(this.grid, 4, 4);

    this.initGraph();
    Graph c = new Utils().makeConnections(this.grid, 4, 4);

    this.initGraph();
    Graph r = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo minV = a.findVerticalSeam();
    // rip seam first time
    a.ripSeamVertical(minV);
    // get min 2nd time
    SeamInfo min2V = a.findVerticalSeam();
    // rip seam 2nd time
    a.ripSeamVertical(min2V);

    // reinsert the last two removed seams and checking connectivity
    a.reinsert();
    a.reinsert();

    c.ripSeamVertical(c.findVerticalSeam());
    c.ripSeamVertical(c.findVerticalSeam());
    c.reinsert();

    r.ripSeamVertical(r.findVerticalSeam());

    // the graph with two removed vertical seams and reinserted twice has the same
    // connections as the original
    return t.checkExpect(g.topLeft, f.topLeft)
        // removing one vertical seam from the original is the same connections as
        // removing two
        // vertical seams and reinserting one from the original
        && t.checkExpect(h.topLeft, d.topLeft)
        // the graph with two horiz removed seams and reinserted twice has the same
        // connections as the original
        && t.checkExpect(a.topLeft, b.topLeft)
        // removing one horiz seam from the original is the same connections as removing
        // two
        // horiz seams and reinserting one from the original
        && t.checkExpect(c.topLeft, r.topLeft);

  }

  boolean testReInsertSeamVertical(Tester t) {

    this.initGraph();

    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findVerticalSeam();
    // rip seam first time
    g.ripSeamVertical(min);

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, Color.white);
    afterOneSeam.setColorAt(1, 2, Color.white);
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 3, new Color(142, 207, 242));

    // get min 2nd time
    SeamInfo min2 = g.findVerticalSeam();
    // rip seam 2nd time
    g.ripSeamVertical(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, Color.white);
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 2, Color.white);
    afterTwoSeams.setColorAt(1, 2, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 3, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 3, new Color(142, 207, 242));

    g.reinsert();

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }

  boolean testReInsertSeamHelpVertical(Tester t) {

    this.initGraph();

    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findVerticalSeam();

    APixel curr = min.pixel;
    curr.south.fixVerticalBorder();

    curr.west.east = curr.east;
    curr.east.west = curr.west;

    g.width -= 1;

    g.ripSeamVerticalHelp(min);

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(3, 4);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, Color.white);
    afterOneSeam.setColorAt(1, 2, Color.white);
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 3, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 3, new Color(142, 207, 242));

    // get min 2nd time
    SeamInfo min2 = g.findVerticalSeam();

    APixel curr2 = min2.pixel;
    curr2.south.fixVerticalBorder();

    curr2.west.east = curr2.east;
    curr2.east.west = curr2.west;

    g.width -= 1;

    g.ripSeamVerticalHelp(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(2, 4);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, Color.white);
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 2, Color.white);
    afterTwoSeams.setColorAt(1, 2, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 3, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 3, new Color(142, 207, 242));

    g.reinsert();

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }

  boolean testReinsertSeamHorizontal(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findHorizontalSeam();
    // rip seam first time
    g.ripSeamHorizontal(min);

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 2, new Color(142, 207, 242));
    // get min 2nd time
    SeamInfo min2 = g.findHorizontalSeam();
    // rip seam 2nd time
    g.ripSeamHorizontal(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 1, new Color(142, 207, 242));

    g.reinsert();
    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }

  boolean testReinsertSeamHelpHorizontal(Tester t) {
    this.initGraph();
    Graph g = new Utils().makeConnections(this.grid, 4, 4);

    // get min
    SeamInfo min = g.findHorizontalSeam();

    APixel curr = min.pixel;
    curr.east.fixHorizontalBorder();

    curr.north.south = curr.south;
    curr.south.north = curr.north;

    g.ripSeamHorizontalHelp(min);

    g.height -= 1;

    ComputedPixelImage afterOneSeam = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterOneSeam.setColorAt(0, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 0, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 0, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 1, Color.white);
    afterOneSeam.setColorAt(1, 1, Color.white);
    afterOneSeam.setColorAt(2, 1, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 1, new Color(142, 207, 242));

    afterOneSeam.setColorAt(0, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(1, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(2, 2, new Color(142, 207, 242));
    afterOneSeam.setColorAt(3, 2, new Color(142, 207, 242));

    // get min 2nd time
    SeamInfo min2 = g.findHorizontalSeam();

    APixel curr2 = min2.pixel;
    curr2.east.fixHorizontalBorder();

    curr2.north.south = curr2.south;
    curr2.south.north = curr2.north;

    g.height -= 1;

    g.ripSeamHorizontalHelp(min2);

    ComputedPixelImage afterTwoSeams = new ComputedPixelImage(g.width - 2, g.height - 2);
    afterTwoSeams.setColorAt(0, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 0, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 0, new Color(142, 207, 242));

    afterTwoSeams.setColorAt(0, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(1, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(2, 1, new Color(142, 207, 242));
    afterTwoSeams.setColorAt(3, 1, new Color(142, 207, 242));

    g.reinsert();

    // check if rendering (which simply iterates over the graph, rendering a pixel
    // at a time) shows that a the
    // correct seam has been removed
    return t.checkExpect(g.render(), afterOneSeam);
  }
  
  
  boolean testOnTick(Tester t) {
    
    SeamCarverWorld world = new SeamCarverWorld(new FromFileImage("3x4.png"));
    
    SeamCarverWorld worldResult = new SeamCarverWorld(new FromFileImage("2x4.png"));
    
    world.onTickForTesting(new Random(3));
    world.onTickForTesting(new Random(3));
    
    return t.checkExpect(world, worldResult);
    
  }
  
  

  
}
