/*******************************************************************************
 * Copyright (c) 2015 Christian Hensel
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Christian Hensel - initial API and implementation
 *******************************************************************************/
package edu.logic.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Static helper class, handling pixel operations on {@link BufferedImage}.
 *
 * @author Christian
 *
 */
public class ImageUtil
{
   // Indices for each of the 4 triangle areas of an image
   public final static int TOP_AREA_INDEX = 0;
   public final static int RIGHT_AREA_INDEX = 1;
   public final static int BOTTOM_AREA_INDEX = 2;
   public final static int LEFT_AREA_INDEX = 3;


   private ImageUtil()
   {
      // static only
   }


   /**
    * Generate random sample from random locations of the input image.
    *
    * @param inputImage
    *           The image to crop a sample from.
    * @param tileResolution
    *           Width and height of sample.
    * @param sampleCount
    *           Number of samples
    * @return List of samples
    */
   public static List<BufferedImage> generateSamples(BufferedImage inputImage, int tileResolution, int sampleCount)
   {
      Random rand = new Random();

      List<BufferedImage> subImages = new ArrayList<BufferedImage>();

      for (int i = 0; i < sampleCount; i++)
      {
         int x = rand.nextInt(inputImage.getWidth() - tileResolution + 1);
         int y = rand.nextInt(inputImage.getHeight() - tileResolution + 1);

         subImages.add(ImageUtil.crop(inputImage, x, y, tileResolution, tileResolution));
      }

      return subImages;
   }


   /**
    * Moves all pixel of inputImage in up direction. Fills the most bottom lines of pixel with the lines of those pixels
    * that got pushed off the border.
    */
   public static BufferedImage moveUpHalfway(BufferedImage inputImage)
   {
      Graphics inputGraphic = inputImage.getGraphics();
      int resolution = inputImage.getHeight();
      int offset = resolution / 2;

      for (int y = 0; y < offset; y++)
      {
         for (int x = 0; x < resolution; x++)
         {
            Color topColor = new Color(inputImage.getRGB(x, y));
            Color bottomColor = new Color(inputImage.getRGB(x, y + offset));

            inputGraphic.setColor(topColor);
            inputGraphic.drawLine(x, y + offset, x, y + offset);
            inputGraphic.setColor(bottomColor);
            inputGraphic.drawLine(x, y, x, y);
         }
      }
      return inputImage;
   }


   /**
    * Moves all pixel of inputImage in right direction. Fills the most left lines of pixel with the lines of those pixels
    * that got pushed off the border.
    */
   public static BufferedImage moveRightHalfway(BufferedImage inputImage)
   {
      Graphics inputGraphic = inputImage.getGraphics();
      int resolution = inputImage.getHeight();
      int offset = resolution / 2;

      for (int y = 0; y < resolution; y++)
      {
         for (int x = 0; x < offset; x++)
         {
            Color leftColor = new Color(inputImage.getRGB(x, y));
            Color rightColor = new Color(inputImage.getRGB(x + offset, y));

            inputGraphic.setColor(leftColor);
            inputGraphic.drawLine(x + offset, y, x + offset, y);
            inputGraphic.setColor(rightColor);
            inputGraphic.drawLine(x, y, x, y);
         }
      }
      return inputImage;
   }


   /**
    * Returns an index indicating the area (top, right, bottom, left) of the image.
    * Assumes image is quadratic -> width = height.
    *
    * @param x
    *           Coordinate
    * @param y
    *           Coordinate
    * @param imageResolution
    *           Width and height of image.
    * @return 0 for top, 1 for Right, 2 for Bottom, 3 for left
    */
   public static int getAreaIndex(int x, int y, int imageResolution)
   {
      int halfTileResolution = imageResolution / 2;

      if (x < halfTileResolution)
      {
         if (y < halfTileResolution) // Upper left area of square
         {
            if (x > y) // Above diagonale -> Top
            {
               return TOP_AREA_INDEX;
            }
            else
            // Below or exactly on diagonale -> Left
            {
               return LEFT_AREA_INDEX;
            }
         }
         else
         // Bottom left area
         {
            if (x >= (imageResolution - 1) - y) // Below or exactly on diagonale -> Bottom
            {
               return BOTTOM_AREA_INDEX;
            }
            else
            // Above diagonle -> Left
            {
               return LEFT_AREA_INDEX;
            }
         }
      }
      else
      {
         if (y < halfTileResolution) // Top right area of square
         {
            if ((imageResolution - 1) - x >= y) // Above or exactly on diagonale -> Top
            {
               return TOP_AREA_INDEX;
            }
            else
            // Below diagonale -> Right
            {
               return RIGHT_AREA_INDEX;
            }
         }
         else
         // Bottom right area
         {
            if (x >= y) // Above or exactly on diagonale -> Right
            {
               return RIGHT_AREA_INDEX;
            }
            else
            // Below diagonale -> Bottom
            {
               return BOTTOM_AREA_INDEX;
            }
         }
      }
   }


   /**
    * Helper methode to create a sub copy of the src input image.
    * Unfortunately BufferedImage.subImage - Methode returns the same data Array of input image -> changes in subimage would lead
    * to changes in inputImage.
    *
    * @param src
    *           The input image
    * @param x
    *           Upper left point
    * @param y
    *           Upper left point
    * @param width
    *           of sub image
    * @param height
    *           of sub image
    * @return copy of input image
    */
   public static BufferedImage crop(BufferedImage src, int x, int y, int width, int height)
   {
      BufferedImage returnImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics returnGraphics = returnImage.createGraphics();

      for (int i = 0; i < height; i++)
      {
         for (int j = 0; j < width; j++)
         {
            returnGraphics.setColor(new Color(src.getRGB(x + j, y + i)));
            returnGraphics.drawLine(j, i, j, i);
         }
      }
      return returnImage;
   }


   /**
    * Make sure to stay within maximum.
    */
   public static int wrap(int x, int maximum)
   {
      while (x < 0)
      {
         x += maximum;
      }
      while (x >= maximum)
      {
         x -= maximum;
      }
      return x;
   }


   /**
    * Length of a vector.
    */
   public static double length(float[] color)
   {
      return Math.sqrt(color[0] * color[0] + color[1] * color[1] + color[2] * color[2]);
   }
}
