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
package edu.logic.graphs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import edu.logic.util.GraphNodeHorizontalComparator;
import edu.logic.util.GraphNodeVerticalComparator;
import edu.logic.util.ImageUtil;


// TODO: Kommentare anpassen -> auf dijkstra umstellen
/**
 * Base class to create a pixel graph.
 * Uses JGraphT Dijkstra-Implementation to find the lowest cost path within the graph
 * and merges the edge and sample image using the results of the cut.
 *
 * @author Christian
 */
public abstract class BaseAreaGraph
{
   protected GraphNode[][] graphNodes;

   protected final GraphNode firstNode;
   protected final GraphNode secondNode;

   /**
    * Width / Height of the image.
    */
   protected int tileResolution;


   public BaseAreaGraph(GraphNode[][] graphNodes, int tileResolution, GraphNode firstNode, GraphNode secondNode)
   {
      this.graphNodes = graphNodes;
      this.tileResolution = tileResolution;

      this.firstNode = firstNode;
      this.secondNode = secondNode;
   }


   /**
    * Creates edges for pairs of GraphNodes within the respective triangle area of the images.
    * Then applies the JGraphT Dijkstra in order to find the lowest cost path within the graph.
    * Returns a set of keys in the end. Each key is a string consisting of the x and y coordinate of each pixel, which has been
    * choosen by the graphcut algorithm to be a pixel from the edgetile.
    */
   public void execute(Graphics resultingGraphic, BufferedImage edgeTile, BufferedImage sampleTile)
   {
      SimpleWeightedGraph<GraphNode, DefaultWeightedEdge> graph = generate(edgeTile, sampleTile);

      TreeSet<GraphNode> bestPath = cut(graph);

      Set<GraphNode> edgeNodes = getEdgeGraphNodes(bestPath.iterator());

      merge(resultingGraphic, edgeNodes, bestPath, edgeTile, sampleTile);
   }



   /**
    * Creates edges for pairs of GraphNodes within the respective triangle area of the images.
    */
   protected abstract SimpleWeightedGraph<GraphNode, DefaultWeightedEdge> generate(BufferedImage edgeTile, BufferedImage sampleImage);


   /**
    * Applies JGraphT's Dijkstra Implementation -> Finding the lowest cost path within the graph.
    * The result is a treeset of {@link GraphNode} that lie on this path. These nodes are ordered horizontially, if in top or
    * bottom area of graph and vertically if on the right or left side.
    */
   private TreeSet<GraphNode> cut(SimpleWeightedGraph<GraphNode, DefaultWeightedEdge> graph)
   {
      // Find the lowest cost path using dijkstra algorithm
      List<DefaultWeightedEdge> bestPath = DijkstraShortestPath.findPathBetween(graph, firstNode, secondNode);

      // Sort results based on graph logic.
      // This is very ugly, but i am lazy.
      Comparator<GraphNode> comparator = null;
      if (this instanceof RightAreaGraph || this instanceof LeftAreaGraph)
      {
         comparator = new GraphNodeVerticalComparator();
      }
      else if (this instanceof TopAreaGraph || this instanceof BottomAreaGraph)
      {
         comparator = new GraphNodeHorizontalComparator();
      }
      else
      {
         try
         {
            throw new Exception("This type of graph is not supported.");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      TreeSet<GraphNode> pathNodes = new TreeSet<GraphNode>(comparator);
      for (DefaultWeightedEdge edge : bestPath)
      {
         pathNodes.add(graph.getEdgeSource(edge));
         pathNodes.add(graph.getEdgeTarget(edge));
      }
      return pathNodes;
   }


   /**
    * Returns a set of {@link GraphNode}. Each pixel with the same location as the {@link GraphNode} will be choosen from the
    * edgetile.
    */
   protected abstract Set<GraphNode> getEdgeGraphNodes(Iterator<GraphNode> bestPath);


   /**
    * Uses the 4 graphcuts to merge the sample and edgetile into one final resulting tile.
    */
   protected abstract void merge(Graphics resultingGraphic, Set<GraphNode> edgeNodes, TreeSet<GraphNode> bestPath, BufferedImage edgeTile, BufferedImage sampleTile);


   /**
    * Creates a weighted edge between two {@link GraphNode}.
    */
   protected void createEdge(SimpleWeightedGraph<GraphNode, DefaultWeightedEdge> graph, BufferedImage edgeTile, BufferedImage sampleImage, GraphNode left, GraphNode right)
   {
      if (right == null)
      {
         return;
      }
      graph.addVertex(right);

      double edgeWeight = calculateEdgeWeight(edgeTile, sampleImage, left, right);
      graph.setEdgeWeight(graph.addEdge(left, right), edgeWeight);
   }


   /**
    * Calculates the weight between two {@link GraphNode}. The edges of the graph are assigned infinite weights (boundries).
    */
   protected double calculateEdgeWeight(BufferedImage edgeTile, BufferedImage sampleImage, GraphNode left, GraphNode right)
   {
      // Edges between most left and most right nodes have infinite edge weight
      if ((left.getX() == 0 || right.getX() == tileResolution - 1) && left.getY() != right.getY())
      {
         return Double.MAX_VALUE;
      }
      else if ((left.getY() == 0 || right.getY() == tileResolution - 1) && left.getX() != right.getX())
      // Edges between most upper and most bottom nodes have infinite edge weight
      {
         return Double.MAX_VALUE;
      }
      else if (tileResolution - right.getX() - 1 == right.getY()) // Left Diagonale
      {
         return Double.MAX_VALUE;
      }
      else if (right.getX() == right.getY()) // Right Diagonale
      {
         return Double.MAX_VALUE;
      }
      else
      // Any other edge
      {
         // Calculate difference between edge and sample pixel at left location
         Color As = new Color(edgeTile.getRGB(left.getX(), left.getY()));
         Color Bs = new Color(sampleImage.getRGB(left.getX(), left.getY()));
         double AsLength = ImageUtil.length(new float[] { As.getRed(), As.getGreen(), As.getBlue() });
         double BsLength = ImageUtil.length(new float[] { Bs.getRed(), Bs.getGreen(), Bs.getBlue() });
         double sLength = AsLength > BsLength ? AsLength - BsLength : BsLength - AsLength;

         // Calculate difference between edge and sample pixel at right location
         Color At = new Color(edgeTile.getRGB(right.getX(), right.getY()));
         Color Bt = new Color(sampleImage.getRGB(right.getX(), right.getY()));
         double AtLength = ImageUtil.length(new float[] { At.getRed(), At.getGreen(), At.getBlue() });
         double BtLength = ImageUtil.length(new float[] { Bt.getRed(), Bt.getGreen(), Bt.getBlue() });
         double tLength = AtLength > BtLength ? AtLength - BtLength : BtLength - AtLength;

         return sLength + tLength;
      }
   }


   /**
    * If edgeNodes contains node, returns the color of the pixel of the edgeTile at x,y-location.
    * Else returns pixel of sampleTile.
    * If bestPath contains node, a median color of the surrounding pixels of edgeTile and sampleTile is returned in order
    * to reduce the visibility of the seam between edge- and sampleTile and thus further improve the quality of the results.
    */
   protected Color getColor(GraphNode node, Set<GraphNode> edgeNodes, TreeSet<GraphNode> bestPath, BufferedImage edgeTile, BufferedImage sampleTile)
   {
      // If node not exactly on bestPath
      if (!bestPath.contains(node))
      {
         return edgeNodes.contains(node) ? new Color(edgeTile.getRGB(node.getX(), node.getY())) : new Color(sampleTile.getRGB(node.getX(), node.getY()));
      }

      // Kill seam by replacing the color of the node with the median of its surrounding pixels
      int counter = 0;
      int r = 0, g = 0, b = 0;

      Set<GraphNode> neighBors = new HashSet<GraphNode>();
      GraphNode rightNeighBor = node.getRightNeighbor();
      GraphNode bottomNeighBor = node.getBottomNeighbor();
      GraphNode leftNeighBor = node.getLeftNeighbor();
      GraphNode topNeighBor = node.getTopNeighbor();
      if (rightNeighBor != null)
      {
         neighBors.add(rightNeighBor);
      }
      if (bottomNeighBor != null)
      {
         neighBors.add(bottomNeighBor);
      }
      if (leftNeighBor != null)
      {
         neighBors.add(leftNeighBor);
      }
      if (topNeighBor != null)
      {
         neighBors.add(topNeighBor);
      }
      if (rightNeighBor != null)
      {
         if (topNeighBor != null)
         {
            neighBors.add(topNeighBor.getRightNeighbor());
         }
         if (bottomNeighBor != null)
         {
            neighBors.add(bottomNeighBor.getRightNeighbor());
         }
      }
      if (leftNeighBor != null)
      {
         if (topNeighBor != null)
         {
            neighBors.add(topNeighBor.getLeftNeighbor());
         }
         if (bottomNeighBor != null)
         {
            neighBors.add(bottomNeighBor.getLeftNeighbor());
         }
      }

      for (GraphNode neighBor : neighBors)
      {
         Color color = edgeNodes.contains(neighBor) ? new Color(edgeTile.getRGB(neighBor.getX(), neighBor.getY())) : new Color(sampleTile.getRGB(neighBor.getX(), neighBor.getY()));
         r += color.getRed();
         g += color.getGreen();
         b += color.getBlue();
         counter++;
      }

      r /= counter;
      g /= counter;
      b /= counter;

      return new Color(r, g, b);
   }
}
