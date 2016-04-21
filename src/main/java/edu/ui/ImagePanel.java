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
package edu.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


/**
 * Helper panel to handle and display a {@link BufferedImage}
 *
 * @author Christian
 */
class ImagePanel extends JPanel
{
   // The input image
   private BufferedImage inputImage;

   // The scaled input image
   private Image scaledImage;


   @Override
   public void invalidate()
   {
      scaledImage = null;
      super.invalidate();
   }


   public void reset()
   {
      inputImage = null;
      scaledImage = null;
      repaint();
   }


   public void setImage(BufferedImage image)
   {
      if (image != null)
      {
         inputImage = image;
         scaledImage = inputImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
         repaint();
      }
   }


   public BufferedImage getImage()
   {
      return inputImage;
   }


   @Override
   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      if (scaledImage != null)
      {
         Graphics2D g2d = (Graphics2D) g.create();

         int width = getWidth();
         int height = getHeight();

         int x = (width - scaledImage.getWidth(this)) / 2;
         int y = (height - scaledImage.getHeight(this)) / 2;
         g2d.drawImage(scaledImage, x, y, this);
         g2d.dispose();
      }
   }

}