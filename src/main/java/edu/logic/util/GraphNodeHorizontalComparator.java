/**
 * Copyright (c) 2015 Christian Hensel
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Christian Hensel - initial API and implementation
 * *****************************************************************************
 */
package edu.logic.util;

import java.util.Comparator;

import edu.logic.graphs.GraphNode;


/**
 * Comparator that sorts two GraphNodes vertically in ascending order using their X - coordinate.
 * If X - coordinates are equal, the respective Y - coordinates are compared, also in ascending order.
 *
 * @author Christian
 *
 */
public class GraphNodeHorizontalComparator implements Comparator<GraphNode>
{
   @Override
   public int compare(GraphNode top, GraphNode bottom)
   {
      if (top.getX() != bottom.getX())
      {
         return Integer.compare(top.getX(), bottom.getX());
      }
      else
      {
         return Integer.compare(top.getY(), bottom.getY());
      }
   }
}
