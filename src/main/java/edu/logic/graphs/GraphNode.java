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

import edu.logic.util.ImageUtil;



/**
 * A helper class used to represent a node in the graph.
 * Encapsules a location (x, y coordinates), an index indicating to which area of the graph (top, bottom, left, right) this node
 * belongs to and references to the adjacent neighbor GraphNodes.
 *
 * @author Christian
 */
public class GraphNode
{
   // Coordinates
   private final int x, y;

   // Indicates in which area the graph node lies (top, bottom, left, right)
   private final int areaIndex;

   private GraphNode topNeighbor;
   private GraphNode rightNeighbor;
   private GraphNode bottomNeighbor;
   private GraphNode leftNeighbor;


   public GraphNode(int x, int y, int imageResolution)
   {
      this.x = x;
      this.y = y;
      this.areaIndex = ImageUtil.getAreaIndex(x, y, imageResolution);
   }


   public int getX()
   {
      return this.x;
   }


   public int getY()
   {
      return this.y;
   }


   public int getAreaIndex()
   {
      return this.areaIndex;
   }


   public GraphNode getRightNeighbor()
   {
      return rightNeighbor;
   }


   public void setRightNeighbor(GraphNode rightNeighbor)
   {
      this.rightNeighbor = rightNeighbor;
   }


   public GraphNode getBottomNeighbor()
   {
      return bottomNeighbor;
   }


   public void setBottomNeighbor(GraphNode bottomNeighbor)
   {
      this.bottomNeighbor = bottomNeighbor;
   }


   public GraphNode getTopNeighbor()
   {
      return topNeighbor;
   }


   public void setTopNeighbor(GraphNode topNeighbor)
   {
      this.topNeighbor = topNeighbor;
   }


   public GraphNode getLeftNeighbor()
   {
      return leftNeighbor;
   }


   public void setLeftNeighbor(GraphNode leftNeighbor)
   {
      this.leftNeighbor = leftNeighbor;
   }
}