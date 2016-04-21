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
 * Comparator that sorts two GraphNodes vertically in ascending order using their Y - coordinate.
 * If Y - coordinates are equal, the respective X - coordinates are compared, also in ascending order.
 *
 * @author Christian
 *
 */
public class GraphNodeVerticalComparator implements Comparator<GraphNode>
{
   @Override
   public int compare(GraphNode top, GraphNode bottom)
   {
      if (top.getY() != bottom.getY())
      {
         return Integer.compare(top.getY(), bottom.getY());
      }
      else
      {
         return Integer.compare(top.getX(), bottom.getX());
      }
   }
}
