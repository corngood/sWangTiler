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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;


/**
 * Implementation that applies the abstract logic of {@link BaseAreaGraph} on the top side
 * of the triangle area of the images.
 *
 * @author Christian
 */
public class TopAreaGraph extends BaseAreaGraph
{
   public TopAreaGraph(GraphNode[][] graphNodes, int tileResolution)
   {
      super(graphNodes, tileResolution, graphNodes[0][0], graphNodes[tileResolution - 1][0]);
   }


   @Override
   protected SimpleWeightedGraph<GraphNode, DefaultWeightedEdge> generate(BufferedImage edgeTile, BufferedImage sampleTile)
   {
      SimpleWeightedGraph<GraphNode, DefaultWeightedEdge> graph = new SimpleWeightedGraph<GraphNode, DefaultWeightedEdge>(DefaultWeightedEdge.class);

      // Create edges
      int halfTileResolution = tileResolution / 2;

      // Top left part
      int stepCounter = 2;
      for (int y = halfTileResolution - 1; y >= 0; y--)
      {
         for (int x = halfTileResolution - stepCounter; x < halfTileResolution; x++)
         {
            if (x < 0)
            {
               continue;
            }
            GraphNode graphNode = graphNodes[x][y];
            graph.addVertex(graphNode);

            createEdge(graph, edgeTile, sampleTile, graphNode, graphNode.getRightNeighbor());
            createEdge(graph, edgeTile, sampleTile, graphNode, graphNode.getTopNeighbor());
         }
         stepCounter++;
      }

      // Top right part
      stepCounter = 1;
      for (int y = halfTileResolution - 1; y >= 0; y--)
      {
         for (int x = halfTileResolution + stepCounter; x >= halfTileResolution; x--)
         {
            if (x > tileResolution - 1)
            {
               continue;
            }
            GraphNode graphNode = graphNodes[x][y];
            graph.addVertex(graphNode);

            if (x != halfTileResolution)
            {
               createEdge(graph, edgeTile, sampleTile, graphNode, graphNode.getLeftNeighbor());
            }

            createEdge(graph, edgeTile, sampleTile, graphNode, graphNode.getTopNeighbor());

         }
         stepCounter++;
      }

      return graph;
   }


   @Override
   protected double calculateEdgeWeight(BufferedImage edgeTile, BufferedImage sampleTile, GraphNode left, GraphNode right)
   {
      // Graphcut exit points
      if (left.getY() == 0 && (left.getX() == 0 || left.getX() == tileResolution - 1))
      {
         return 0;
      }
      return super.calculateEdgeWeight(edgeTile, sampleTile, left, right);
   }


   @Override
   protected Set<GraphNode> getEdgeGraphNodes(Iterator<GraphNode> bestPath)
   {
      Set<GraphNode> edgeNodes = new HashSet<GraphNode>();
      GraphNode cutNode = null;
      GraphNode helper = bestPath.next();
      for (int x = 0; x < tileResolution; x++)
      {
         // Find the bottom most cutpoint for this X-Coordinate
         // Skip GraphNodes that lie on the same vertical line as the bottom most GraphNode
         while (helper.getX() == x && bestPath.hasNext())
         {
            cutNode = helper;
            helper = bestPath.next();
         }
         // All GraphNodes whose Y-Coordinate are equal or less than the cutPoint are added to the result.
         for (int y = 0; y <= cutNode.getY(); y++)
         {
            edgeNodes.add(graphNodes[x][y]);
         }
      }
      // Add corners for better quality
      edgeNodes.add(graphNodes[0][0]);
      edgeNodes.add(graphNodes[tileResolution - 1][0]);
      edgeNodes.add(graphNodes[1][1]);
      edgeNodes.add(graphNodes[tileResolution - 2][1]);
      return edgeNodes;
   }


   @Override
   protected void merge(Graphics resultingGraphic, Set<GraphNode> edgeNodes, TreeSet<GraphNode> bestPath, BufferedImage edgeTile, BufferedImage sampleTile)
   {
      int halfTileResolution = tileResolution / 2;

      // Top left part
      int stepCounter = 2;
      for (int y = halfTileResolution - 1; y >= 0; y--)
      {
         for (int x = halfTileResolution - stepCounter; x < halfTileResolution; x++)
         {
            if (x < 0)
            {
               continue;
            }

            Color color = getColor(graphNodes[x][y], edgeNodes, bestPath, edgeTile, sampleTile);
            resultingGraphic.setColor(color);
            resultingGraphic.drawLine(x, y, x, y);
         }
         stepCounter++;
      }

      // Top right part
      stepCounter = 1;
      for (int y = halfTileResolution - 1; y >= 0; y--)
      {
         for (int x = halfTileResolution + stepCounter; x >= halfTileResolution; x--)
         {
            if (x > tileResolution - 1)
            {
               continue;
            }

            Color color = getColor(graphNodes[x][y], edgeNodes, bestPath, edgeTile, sampleTile);
            resultingGraphic.setColor(color);
            resultingGraphic.drawLine(x, y, x, y);
         }
         stepCounter++;
      }
   }
}
