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
package edu.logic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.logic.graphs.BaseAreaGraph;
import edu.logic.graphs.BottomAreaGraph;
import edu.logic.graphs.GraphNode;
import edu.logic.graphs.LeftAreaGraph;
import edu.logic.graphs.RightAreaGraph;
import edu.logic.graphs.TopAreaGraph;
import edu.logic.util.ImageUtil;
import edu.ui.ApplicationWindow;


/**
 * The core of the program.
 * Uses strict wang tiling algorithm as described in
 * http://graphics.ewha.ac.kr/SWangTile/main.pdf
 *
 * @author Christian
 */
public final class StrictWangTileGenerator
{
   private final static int YELLOW = 0;
   private final static int GREEN = 1;
   private final static int BLUE = 2;
   private final static int RED = 3;

   private final static int NUMBER_OF_EDGES = 4;

   // The Object to send my results to
   private final ApplicationWindow employer;
   // Number of tiles
   private final int numberOfTiles;
   // Width and height of the to be generated tiles
   private final int tileResolution;
   // Sample images
   private final List<BufferedImage> sampleTiles;
   // Edge images
   private final List<BufferedImage> edgeTiles;
   // One graph for each side (top, right, bottom, left)
   private final List<? extends BaseAreaGraph> graphs;


   public StrictWangTileGenerator(ApplicationWindow employer, BufferedImage inputImage, int numberOfTiles, int tileResolution)
   {
      this.numberOfTiles = numberOfTiles;
      this.tileResolution = tileResolution;
      this.employer = employer;

      // Generate random samples
      sampleTiles = ImageUtil.generateSamples(inputImage, tileResolution, numberOfTiles);

      // Generate edgeTiles using combinations of edge images
      edgeTiles = generateInitialEdgeTiles(ImageUtil.generateSamples(inputImage, tileResolution, NUMBER_OF_EDGES), getEdgeTileIndices(numberOfTiles));

      // For each tile: find min cut paths between sampleTile and tile overlay in each of the 4 areas
      graphs = createGraphs(tileResolution);
   }


   /**
    * Core function. Generates tile.
    *
    * @return a new Tile.
    */
   public void generate()
   {
      ExecutorService executorService = Executors.newSingleThreadExecutor();
      List<Future<BufferedImage>> futures = new ArrayList<Future<BufferedImage>>();
      for (int i = 0; i < numberOfTiles; i++)
      {
         final int iteration = i;
         futures.add(executorService.submit(new Callable<BufferedImage>()
         {
            @Override
            public BufferedImage call() throws Exception
            {
               BufferedImage edgeTile = edgeTiles.get(iteration);
               BufferedImage sampleTile = sampleTiles.get(iteration);

               // For each triangle area of the image: Create the graph, find the lowest cost path
               // and use those information to merge the edge and sample image.
               BufferedImage newTile = new BufferedImage(tileResolution, tileResolution, BufferedImage.TYPE_INT_ARGB);
               Graphics returnGraphic = newTile.createGraphics();

               for (BaseAreaGraph areaGraph : graphs)
               {
                  areaGraph.execute(returnGraphic, edgeTile, sampleTile);
               }

               return newTile;
            }
         }));
      }
      for (int i = 0; i < numberOfTiles; i++)
      {
         try
         {
            employer.setImage(futures.get(i).get(), i);
         }
         catch (InterruptedException | ExecutionException e)
         {
            e.printStackTrace();
         }
      }
   }


   /**
    * Combines the edge images into 8 initial images.
    */
   private List<BufferedImage> generateInitialEdgeTiles(List<BufferedImage> edgeTiles, int[][] edgeImageIndices)
   {
      // Move pixels of edgeImages up / right by half the images width to allow seamless tiling
      BufferedImage yellowEdgeTile = ImageUtil.moveUpHalfway(edgeTiles.get(YELLOW));
      BufferedImage greenEdgeTile = ImageUtil.moveRightHalfway(edgeTiles.get(GREEN));
      BufferedImage blueEdgeTile = ImageUtil.moveUpHalfway(edgeTiles.get(BLUE));
      BufferedImage redEdgeTile = ImageUtil.moveRightHalfway(edgeTiles.get(RED));
      edgeTiles = Arrays.asList(yellowEdgeTile, greenEdgeTile, blueEdgeTile, redEdgeTile);

      List<BufferedImage> combinedEdgeTiles = new ArrayList<BufferedImage>();
      for (int i = 0; i < numberOfTiles; i++)
      {
         BufferedImage newTile = new BufferedImage(tileResolution, tileResolution, BufferedImage.TYPE_INT_ARGB);
         Graphics returnGraphics = newTile.createGraphics();
         for (int y = 0; y < tileResolution; y++)
         {
            for (int x = 0; x < tileResolution; x++)
            {
               // Get index of correct edgeImage derived from x and y coordinate
               int idx = edgeImageIndices[i][ImageUtil.getAreaIndex(x, y, tileResolution)];
               BufferedImage subImage = edgeTiles.get(idx);
               returnGraphics.setColor(new Color(subImage.getRGB(x, y)));
               returnGraphics.drawLine(x, y, x, y);
            }
         }
         combinedEdgeTiles.add(newTile);
      }
      return combinedEdgeTiles;
   }


   /**
    * Get different combinations of edge images indicated by color.
    *
    * @return 2D - int array.
    */
   public static int[][] getEdgeTileIndices(int numberOfTiles)
   {
      if (numberOfTiles == 4)
      {
         return new int[][] {
                             new int[] { YELLOW, GREEN, BLUE, RED },
                             new int[] { YELLOW, RED, BLUE, GREEN },
                             new int[] { BLUE, GREEN, YELLOW, RED },
                             new int[] { BLUE, GREEN, BLUE, GREEN } };
      }
      else if (numberOfTiles == 8)
      {
         return new int[][] {
                             new int[] { YELLOW, GREEN, BLUE, RED },
                             new int[] { BLUE, RED, BLUE, RED },
                             new int[] { YELLOW, GREEN, YELLOW, GREEN },
                             new int[] { BLUE, RED, YELLOW, GREEN },
                             new int[] { YELLOW, RED, BLUE, GREEN },
                             new int[] { BLUE, GREEN, BLUE, GREEN },
                             new int[] { YELLOW, RED, YELLOW, RED },
                             new int[] { BLUE, GREEN, YELLOW, RED } };
      }
      else if (numberOfTiles == 16)
      {
         return new int[][] {
                             new int[] { YELLOW, RED, BLUE, RED },
                             new int[] { YELLOW, GREEN, BLUE, RED },
                             new int[] { YELLOW, GREEN, BLUE, GREEN },
                             new int[] { YELLOW, RED, BLUE, GREEN },
                             new int[] { BLUE, RED, BLUE, RED },
                             new int[] { BLUE, GREEN, BLUE, RED },
                             new int[] { BLUE, GREEN, BLUE, GREEN },
                             new int[] { BLUE, RED, BLUE, GREEN },
                             new int[] { BLUE, RED, YELLOW, RED },
                             new int[] { BLUE, GREEN, YELLOW, RED },
                             new int[] { BLUE, GREEN, YELLOW, GREEN },
                             new int[] { BLUE, RED, YELLOW, GREEN },
                             new int[] { YELLOW, RED, YELLOW, RED },
                             new int[] { YELLOW, GREEN, YELLOW, RED },
                             new int[] { YELLOW, GREEN, YELLOW, GREEN },
                             new int[] { YELLOW, RED, YELLOW, GREEN } };
      }
      else
      {
         throw new IllegalArgumentException("Number of tiles not supported.");
      }
   }


   /**
    * @return A list of graphs one for each triangle area of the image.
    */
   private List<? extends BaseAreaGraph> createGraphs(int tileResolution)
   {
      // Create nodes
      GraphNode[][] graphNodes = new GraphNode[tileResolution][tileResolution];
      for (int y = 0; y < tileResolution; y++)
      {
         for (int x = 0; x < tileResolution; x++)
         {
            GraphNode newGraphNode = new GraphNode(x, y, tileResolution);
            graphNodes[x][y] = newGraphNode;
         }
      }
      // Create neighbor relations
      for (int y = 0; y < tileResolution; y++)
      {
         for (int x = 0; x < tileResolution; x++)
         {
            if (x != 0)
            {
               graphNodes[x][y].setLeftNeighbor(graphNodes[x - 1][y]);
            }
            if (x != tileResolution - 1)
            {
               graphNodes[x][y].setRightNeighbor(graphNodes[x + 1][y]);
            }
            if (y != 0)
            {
               graphNodes[x][y].setTopNeighbor(graphNodes[x][y - 1]);
            }
            if (y != tileResolution - 1)
            {
               graphNodes[x][y].setBottomNeighbor(graphNodes[x][y + 1]);
            }
         }
      }

      return Arrays.asList(new TopAreaGraph(graphNodes, tileResolution),
                           new RightAreaGraph(graphNodes, tileResolution),
                           new BottomAreaGraph(graphNodes, tileResolution),
                           new LeftAreaGraph(graphNodes, tileResolution));
   }
}
