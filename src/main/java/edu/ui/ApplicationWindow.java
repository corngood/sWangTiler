/*******************************************************************************
 * Copyright (c) 2015 Christian Hensel
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Christian Hensel - initial API and implementation
 *******************************************************************************/
package edu.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import edu.logic.StrictWangTileGenerator;



/**
 * GUI Main Window. -> Initializes all gui components and handles events.
 *
 * @author Christian
 */
public class ApplicationWindow extends JFrame implements ActionListener
{
   // Allowed number of tiles
   public final static List<Integer> ALLOWED_NUMBER_OF_TILES = Arrays.asList(new Integer[] { 4, 8, 16 });
   // Allowed tile resolutions
   public final static List<Integer> ALLOWED_RESOLUTIONS = Arrays.asList(new Integer[] { 16, 32, 64, 128  });

   // Application title
   private final static String TITLE = "Wang Tiler";
   // Format of saved tiles
   private final static String TILEFORMAT = "png";
   // Location of image sample folder
   private final static String IMAGEFOLDER = "images";

   // A filechooser dialog
   private JFileChooser imageFileChooser;
   // The corresponding button to open the filechooser.
   private JButton imageFileChooserButton;
   // Panel container for the choosen image.
   private ImagePanel imagePanel;
   // Number tiles label
   private JLabel numberTilesLabel;
   // Number of tiles combobox
   private JComboBox<String> numberTilesComboBox;
   // Tile size label
   private JLabel tileSizeLabel;
   // Tile size combobox
   private JComboBox<String> tileSizeComboBox;
   // Button that starts tile generation process
   private JButton tilingStartButton;
   // Pack tiles and save - button
   private JButton savePackedButton;
   // Save individual tiles - button
   private JButton saveButton;
   // Generate large sample texture - button
   private JButton sampleTextureButton;
   // Used to demonstrate what the tiles will look like
   // when stacked next to each other in order to form a giant texture
   private ImagePanel sampleTexturePanel;
   // Set of panels for to be generated wang tiles
   private List<ImagePanel> tilePanels;
   // Number of tiles
   private int numberOfTiles;
   // Tile resolution
   private int tileResolution;


   public ApplicationWindow()
   {
      super();

      // Main Window initialization
      setTitle(TITLE);
      setResizable(false);
      setDefaultCloseOperation(EXIT_ON_CLOSE);

      setContentPane(new JPanel());
      getContentPane().setForeground(Color.white);
      getContentPane().setBackground(Color.black);

      // I dont like the layout manager -> they are more hassle
      // than just setting absolute size and position of components manually.
      setLayout(null);

      // Panel container for the choosen image.
      imagePanel = new ImagePanel();
      imagePanel.setBorder(BorderFactory.createLineBorder(Color.gray));
      imagePanel.setBackground(Color.black);
      imagePanel.setBounds(230, 10, 250, 250);
      add(imagePanel);

      // Image chooser dialog
      imageFileChooser = new JFileChooser();
      imageFileChooser.setCurrentDirectory(new File(IMAGEFOLDER));
      imageFileChooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
      imageFileChooser.setAcceptAllFileFilterUsed(false);

      // Image chooser button
      imageFileChooserButton = new JButton("Choose image to tile");
      imageFileChooserButton.addActionListener(this);
      imageFileChooserButton.setBounds(20, 25, 190, 32);
      add(imageFileChooserButton);

      // Tile size text label
      numberTilesLabel = new JLabel("Number of tiles");
      numberTilesLabel.setBounds(20, 155, 100, 30);
      numberTilesLabel.setForeground(Color.white);
      add(numberTilesLabel);

      // Number of tiles combobox
      numberTilesComboBox = new JComboBox<String>();
      numberTilesComboBox.addItem("4");
      numberTilesComboBox.addItem("8");
      numberTilesComboBox.addItem("16");
      numberTilesComboBox.setBounds(130, 160, 80, 25);
      numberTilesComboBox.setSelectedIndex(1);
      numberTilesComboBox.addActionListener(this);
      add(numberTilesComboBox);

      // Tile size text label
      tileSizeLabel = new JLabel("Tile size in pixel");
      tileSizeLabel.setBounds(20, 186, 100, 30);
      tileSizeLabel.setForeground(Color.white);
      add(tileSizeLabel);

      // Tile size combobox
      tileResolution = 64;
      tileSizeComboBox = new JComboBox<String>();
      for (Integer allowedResolution : ALLOWED_RESOLUTIONS)
      {
         // Populate using predefined list
         String allowedSize = allowedResolution.toString();
         tileSizeComboBox.addItem((allowedSize + "x" + allowedSize));
      }
      tileSizeComboBox.setBounds(130, 188, 80, 25);
      tileSizeComboBox.setSelectedIndex(2);
      tileSizeComboBox.addActionListener(this);
      add(tileSizeComboBox);

      // Tiling generation start button
      tilingStartButton = new JButton("Generate Wang tiles");
      tilingStartButton.addActionListener(this);
      tilingStartButton.setBounds(20, 226, 190, 32);
      tilingStartButton.setEnabled(false);
      add(tilingStartButton);

      // tile panels
      tilePanels = new ArrayList<ImagePanel>();
      for (int y = 0; y < 4; y++)
      {
         for (int x = 0; x < 4; x++)
         {
            ImagePanel newTilePanel = new ImagePanel();
            newTilePanel.setBackground(Color.black);
            newTilePanel.setBounds(20 + x * 120, 295 + y * 115, 100, 100);
            tilePanels.add(newTilePanel);
            add(newTilePanel);
         }
      }

      // Save individual tiles - button
      saveButton = new JButton("Save tiles");
      saveButton.addActionListener(this);
      saveButton.setBounds(90, 528, 150, 32);
      saveButton.setEnabled(false);
      add(saveButton);

      // Pack tiles and save - button
      savePackedButton = new JButton("Save packed tiles");
      savePackedButton.addActionListener(this);
      savePackedButton.setBounds(260, 528, 150, 32);
      savePackedButton.setEnabled(false);
      add(savePackedButton);

      // Giant texture panel
      sampleTexturePanel = new ImagePanel();
      sampleTexturePanel.setBorder(BorderFactory.createLineBorder(Color.gray));
      sampleTexturePanel.setBackground(Color.black);
      sampleTexturePanel.setBounds(500, 10, 400, 500);
      add(sampleTexturePanel);

      // Generate large sample texture - button
      sampleTextureButton = new JButton("Generate sample texture");
      sampleTextureButton.addActionListener(this);
      sampleTextureButton.setBounds(600, 528, 200, 32);
      sampleTextureButton.setEnabled(false);
      add(sampleTextureButton);

      // Initialize border color and visibility of tiles
      changeNumberOfTiles();
   }


   @Override
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource() == imageFileChooserButton)// If "Choose image" button has been clicked
      {
         if (importImage() == true)
         {
            tilingStartButton.setEnabled(true);

            for (ImagePanel tilePanel : tilePanels)
            {
               tilePanel.reset();
            }
            sampleTexturePanel.reset();
         }
      }
      else if (e.getSource() == tilingStartButton) // If "Generate Wang tiles" button has been clicked
      {
         generateWangTiles();

         saveButton.setEnabled(true);
         savePackedButton.setEnabled(true);
         sampleTextureButton.setEnabled(true);

         generateSampleTexture();
      }
      else if (e.getSource() == numberTilesComboBox)
      {
         changeNumberOfTiles();
      }
      else if (e.getSource() == tileSizeComboBox)
      {
         // 16, 32, 64, 128
         tileResolution = (int) Math.pow(2, 4 + tileSizeComboBox.getSelectedIndex());
      }
      else if (e.getSource() == saveButton) // If "Save tiles" button has been clicked
      {
         String folderLocation = getFolderLocation();
         saveIndividualTiles(folderLocation);
      }
      else if (e.getSource() == savePackedButton) // If "Save packed tiles" button has been clicked
      {
         String folderLocation = getFolderLocation();
         savePackedTiles(folderLocation);
      }
      else if (e.getSource() == sampleTextureButton)
      {
         generateSampleTexture();
      }
   }


   /**
    * User may choose an image file.
    */
   private boolean importImage()
   {
      // Load and display choosen image file
      if (imageFileChooser.showOpenDialog(ApplicationWindow.this) == JFileChooser.APPROVE_OPTION)
      {
         try
         {
            imagePanel.setImage(ImageIO.read(imageFileChooser.getSelectedFile()));
            return true;
         }
         catch (IOException ex)
         {
            handleException(ex);
         }
      }
      return false;
   }


   /**
    * Changes the number of visible tiles on the gui.
    */
   private void changeNumberOfTiles()
   {
      // 4, 8, 16
      numberOfTiles = (int) (4 * Math.pow(2, numberTilesComboBox.getSelectedIndex()));
      int[][] colorIndices = StrictWangTileGenerator.getEdgeTileIndices(numberOfTiles);

      if (numberOfTiles == 4)
      {
         tilePanels.get(0).setVisible(false);
         tilePanels.get(0).reset();
         tilePanels.get(1).setVisible(true);
         tilePanels.get(1).reset();
         tilePanels.get(1).setBorder(new CompoundBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, toColor(colorIndices[0][0])),
                                                                           BorderFactory.createMatteBorder(0, 0, 0, 2, toColor(colorIndices[0][1]))),
                                                        new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, toColor(colorIndices[0][2])),
                                                                           BorderFactory.createMatteBorder(0, 2, 0, 0, toColor(colorIndices[0][3])))));
         tilePanels.get(2).setVisible(true);
         tilePanels.get(2).reset();
         tilePanels.get(2).setBorder(new CompoundBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, toColor(colorIndices[1][0])),
                                                                           BorderFactory.createMatteBorder(0, 0, 0, 2, toColor(colorIndices[1][1]))),
                                                        new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, toColor(colorIndices[1][2])),
                                                                           BorderFactory.createMatteBorder(0, 2, 0, 0, toColor(colorIndices[1][3])))));
         tilePanels.get(3).setVisible(false);
         tilePanels.get(3).reset();
         tilePanels.get(4).setVisible(false);
         tilePanels.get(4).reset();
         tilePanels.get(5).setVisible(true);
         tilePanels.get(5).reset();
         tilePanels.get(5).setBorder(new CompoundBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, toColor(colorIndices[2][0])),
                                                                           BorderFactory.createMatteBorder(0, 0, 0, 2, toColor(colorIndices[2][1]))),
                                                        new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, toColor(colorIndices[2][2])),
                                                                           BorderFactory.createMatteBorder(0, 2, 0, 0, toColor(colorIndices[2][3])))));
         tilePanels.get(6).setVisible(true);
         tilePanels.get(6).reset();
         tilePanels.get(6).setBorder(new CompoundBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, toColor(colorIndices[3][0])),
                                                                           BorderFactory.createMatteBorder(0, 0, 0, 2, toColor(colorIndices[3][1]))),
                                                        new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, toColor(colorIndices[3][2])),
                                                                           BorderFactory.createMatteBorder(0, 2, 0, 0, toColor(colorIndices[3][3])))));
         tilePanels.get(7).setVisible(false);
         tilePanels.get(7).reset();
         for (int i = 8; i < 16; i++)
         {
            tilePanels.get(i).setVisible(false);
            tilePanels.get(i).reset();
         }

         saveButton.setBounds(90, 528, 150, 32);
         savePackedButton.setBounds(260, 528, 150, 32);

         setBounds(0, 0, 920, 600);
         setLocationRelativeTo(null);
      }
      else if (numberOfTiles == 8)
      {
         for (int i = 0; i < 8; i++)
         {
            tilePanels.get(i).setVisible(true);
            tilePanels.get(i).setBorder(new CompoundBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, toColor(colorIndices[i][0])),
                                                                              BorderFactory.createMatteBorder(0, 0, 0, 2, toColor(colorIndices[i][1]))),
                                                           new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, toColor(colorIndices[i][2])),
                                                                              BorderFactory.createMatteBorder(0, 2, 0, 0, toColor(colorIndices[i][3])))));
            tilePanels.get(i).reset();
         }
         for (int i = 8; i < 16; i++)
         {
            tilePanels.get(i).setVisible(false);
            tilePanels.get(i).reset();
         }

         saveButton.setBounds(90, 528, 150, 32);
         savePackedButton.setBounds(260, 528, 150, 32);

         setBounds(0, 0, 920, 600);
         setLocationRelativeTo(null);
      }
      else if (numberOfTiles == 16)
      {
         for (int i = 0; i < 16; i++)
         {
            tilePanels.get(i).setVisible(true);
            tilePanels.get(i).setBorder(new CompoundBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, toColor(colorIndices[i][0])),
                                                                              BorderFactory.createMatteBorder(0, 0, 0, 2, toColor(colorIndices[i][1]))),
                                                           new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, toColor(colorIndices[i][2])),
                                                                              BorderFactory.createMatteBorder(0, 2, 0, 0, toColor(colorIndices[i][3])))));
            tilePanels.get(i).reset();
         }

         saveButton.setBounds(90, 758, 150, 32);
         savePackedButton.setBounds(260, 758, 150, 32);

         setBounds(0, 0, 920, 830);
         setLocationRelativeTo(null);
      }
      else
      {

         handleException(new NotImplementedException());
      }
   }


   /**
    * Generate wang tiles using {@link StrictWangTileGenerator}.
    */
   private void generateWangTiles()
   {
      BufferedImage inputImage = imagePanel.getImage();

      if (!ALLOWED_NUMBER_OF_TILES.contains(numberOfTiles))
      {
         handleException("Resolution of tile not supported.");
      }
      if (!ALLOWED_RESOLUTIONS.contains(tileResolution))
      {
         handleException("Resolution of tile not supported.");
      }
      if (inputImage.getWidth() < tileResolution)
      {
         handleException("Width of input image (" + inputImage.getWidth() + " px) is too small "
         + "for the desired tile resolution of " + tileResolution + "px.");
      }
      else if (inputImage.getHeight() < tileResolution)
      {
         handleException("Height of input image (" + inputImage.getHeight() + " px) is too small "
         + "for the desired tile resolution of " + tileResolution + "px.");
      }
      else if (inputImage.getWidth() == tileResolution || inputImage.getHeight() == tileResolution)
      {
         handleException("Width or height of input image (" + inputImage.getWidth() + "x" + inputImage.getHeight() + ") equals "
         + "the desired tile resolution of " + tileResolution + "px.\nThis results in tiles that will look all the same!\n\nPlease choose a greater input image or a smaller tile resolution.");
      }
      else
      {
         // Wang generation
         new StrictWangTileGenerator(this, inputImage, numberOfTiles, tileResolution).generate();
      }
   }


   /**
    * Set image for panel -> Return methode of asynchronous call.
    */
   public void setImage(BufferedImage image, int iteration)
   {
      if (numberOfTiles == 4)
      {
         if (iteration == 0)
            iteration = 1;
         else if (iteration == 1)
            iteration = 2;
         else if (iteration == 2)
            iteration = 5;
         else if (iteration == 3)
            iteration = 6;
      }
      ImagePanel panel = tilePanels.get(iteration);
      panel.setImage(image);
      panel.paintComponent(panel.getGraphics());
   }


   /**
    * Combines tiles to one texture and saves to harddrive.
    */
   private void savePackedTiles(String folderLocation)
   {
      List<ImagePanel> helperRef = null;
      int x = 0, y = 0;
      if (numberOfTiles == 4)
      {
         x = 2;
         y = 2;
         helperRef = Arrays.asList(tilePanels.get(1), tilePanels.get(2), tilePanels.get(5), tilePanels.get(6));
      }
      else if (numberOfTiles == 8)
      {
         x = 4;
         y = 2;
         helperRef = tilePanels;
      }
      else if (numberOfTiles == 16)
      {
         x = 4;
         y = 4;
         helperRef = tilePanels;
      }
      else
      {
         handleException(new NotImplementedException());
      }

      BufferedImage packedImage = new BufferedImage(tileResolution * x, tileResolution * y, BufferedImage.TYPE_INT_ARGB);
      Graphics returnGraphics = packedImage.createGraphics();

      for (int j = 0; j < y; j++)
      {
         for (int i = 0; i < x; i++)
         {
            BufferedImage tile = helperRef.get(j * x + i).getImage();
            returnGraphics.drawImage(tile, tileResolution * i, tileResolution * j, tileResolution, tileResolution, null);
         }
      }
      String fileName = folderLocation + "\\tile" + "." + TILEFORMAT;
      saveImage(packedImage, fileName);
   }


   /**
    * Saves tiles to specified location using the following name schema: tile0.png, tile1.png, tile2.png ...
    */
   private void saveIndividualTiles(String folderLocation)
   {
      List<ImagePanel> helperRef = numberOfTiles == 4 ? Arrays.asList(tilePanels.get(1), tilePanels.get(2), tilePanels.get(5), tilePanels.get(6)) : tilePanels;
      for (int i = 0; i < helperRef.size(); i++)
      {
         BufferedImage image = helperRef.get(i).getImage();
         String fileName = folderLocation + "\\tile" + i + "." + TILEFORMAT;
         saveImage(image, fileName);
      }
   }


   /**
    * Saves BufferedImage to harddrive.
    */
   private void saveImage(BufferedImage image, String fileName)
   {
      try
      {
         File saveTileLocation = new File(fileName);
         ImageIO.write(image, TILEFORMAT, saveTileLocation);
      }
      catch (IOException ex)
      {
         handleException(ex);
      }
   }


   /**
    * User may choose a location to save the tiles.
    *
    * @return folder location
    */
   private String getFolderLocation()
   {
      JFileChooser locationChooser = new JFileChooser();
      locationChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
      locationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      locationChooser.setDialogTitle("Choose a directory.");
      if (locationChooser.showOpenDialog(ApplicationWindow.this) == JFileChooser.APPROVE_OPTION)
      {
         return locationChooser.getSelectedFile().getAbsolutePath();
      }

      return null;
   }


   /**
    * Print stacktrace and show message dialog.
    */
   private void handleException(Exception ex)
   {
      handleException(ex.getMessage());
      ex.printStackTrace();
   }


   /**
    * Show error in form of gui dialog.
    */
   private void handleException(String message)
   {
      if (message == null || message.isEmpty())
      {
         return;
      }
      JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.OK_OPTION);
   }


   /**
    * Show demo, what tiles would look like stacked next to each other on a big panel.
    */
   private void generateSampleTexture()
   {
      int[] indices = getOrdering();

      BufferedImage packedImage = new BufferedImage(tileResolution * 4, tileResolution * 6, BufferedImage.TYPE_INT_ARGB);
      Graphics returnGraphics = packedImage.createGraphics();

      for (int y = 0; y < 6; y++)
      {
         for (int x = 0; x < 4; x++)
         {
            BufferedImage tile = tilePanels.get(indices[y * 4 + x]).getImage();
            returnGraphics.drawImage(tile, tileResolution * x, tileResolution * y, tileResolution, tileResolution, null);
         }
      }
      sampleTexturePanel.setImage(packedImage);
   }


   /**
    * Generates a sequence of indices pointing to respective tile.
    */
   private int[] getOrdering()
   {
      Random rand = new Random();

      List<List<Integer>> colorList = getColorLists();
      List<Integer> yellowTop = colorList.get(0);
      List<Integer> blueTop = colorList.get(1);
      List<Integer> greenRight = colorList.get(2);
      List<Integer> redRight = colorList.get(3);
      List<Integer> blueBottom = colorList.get(4);
      List<Integer> yellowBottom = colorList.get(5);
      List<Integer> redLeft = colorList.get(6);
      List<Integer> greenLeft = colorList.get(7);
      List<Integer> yellowTopGreenLeft = colorList.get(8);
      List<Integer> blueTopGreenLeft = colorList.get(9);
      List<Integer> yellowTopRedLeft = colorList.get(10);
      List<Integer> blueTopRedLeft = colorList.get(11);

      List<Integer> indices = new ArrayList<Integer>(4 * 6);
      for (int y = 0; y < 6; y++)
      {
         for (int x = 0; x < 4; x++)
         {
            if (y == 0)
            {
               if (x == 0)
               {
                  indices.add(yellowTop.get(rand.nextInt(yellowTop.size())));
               }
               else
               {
                  Integer leftTile = indices.get(indices.size() - 1);
                  if (greenRight.contains(leftTile))
                  {
                     indices.add(greenLeft.get(rand.nextInt(greenLeft.size())));
                  }
                  else if (redRight.contains(leftTile))
                  {
                     indices.add(redLeft.get(rand.nextInt(redLeft.size())));
                  }
                  else
                  {
                     handleException("An index error occured while trying to generate a sample texture.");
                  }
               }
            }
            else
            {
               Integer topTile = indices.get((y - 1) * 4 + x);
               Integer leftTile = indices.get(indices.size() - 1);

               if (x == 0)
               {
                  if (yellowBottom.contains(topTile))
                  {
                     indices.add(yellowTop.get(rand.nextInt(yellowTop.size())));
                  }
                  else if (blueBottom.contains(topTile))
                  {
                     indices.add(blueTop.get(rand.nextInt(blueTop.size())));
                  }
                  else
                  {
                     handleException("An index error occured while trying to generate a sample texture.");
                  }
               }
               else
               {
                  if (greenRight.contains(leftTile))
                  {
                     if (yellowBottom.contains(topTile))
                     {
                        indices.add(yellowTopGreenLeft.get(rand.nextInt(yellowTopGreenLeft.size())));
                     }
                     else if (blueBottom.contains(topTile))
                     {
                        indices.add(blueTopGreenLeft.get(rand.nextInt(blueTopGreenLeft.size())));
                     }
                     else
                     {
                        handleException("An index error occured while trying to generate a sample texture.");
                     }
                  }
                  else if (redRight.contains(leftTile))
                  {
                     if (yellowBottom.contains(topTile))
                     {
                        indices.add(yellowTopRedLeft.get(rand.nextInt(yellowTopRedLeft.size())));
                     }
                     else if (blueBottom.contains(topTile))
                     {
                        indices.add(blueTopRedLeft.get(rand.nextInt(blueTopRedLeft.size())));
                     }
                     else
                     {
                        handleException("An index error occured while trying to generate a sample texture.");
                     }
                  }
                  else
                  {
                     handleException("An index error occured while trying to generate a sample texture.");
                  }
               }
            }
         }
      }
      int[] results = new int[indices.size()];
      int idx = 0;
      for (int i : indices)
      {
         results[idx++] = i;
      }
      return results;
   }


   /**
    * Returns a List of list of indices of colors.
    */
   public List<List<Integer>> getColorLists()
   {
      List<Integer> yellowTop = null, blueTop = null, greenRight = null, redRight = null, blueBottom = null, yellowBottom = null, redLeft = null, greenLeft = null, yellowTopGreenLeft = null, blueTopGreenLeft = null, yellowTopRedLeft = null, blueTopRedLeft = null;

      if (numberOfTiles == 4)
      {
         yellowTop = Arrays.asList(1, 2);
         blueTop = Arrays.asList(5, 6);
         greenRight = Arrays.asList(1, 5, 6);
         redRight = Arrays.asList(2);
         blueBottom = Arrays.asList(1, 2, 6);
         yellowBottom = Arrays.asList(5);
         redLeft = Arrays.asList(1, 5);
         greenLeft = Arrays.asList(2, 6);
         yellowTopGreenLeft = Arrays.asList(2);
         blueTopGreenLeft = Arrays.asList(6);
         yellowTopRedLeft = Arrays.asList(1);
         blueTopRedLeft = Arrays.asList(5);
      }
      else if (numberOfTiles == 8)
      {
         yellowTop = Arrays.asList(0, 2, 4, 6);
         blueTop = Arrays.asList(1, 3, 5, 7);
         greenRight = Arrays.asList(0, 2, 5, 7);
         redRight = Arrays.asList(1, 3, 4, 6);
         blueBottom = Arrays.asList(0, 1, 4, 5);
         yellowBottom = Arrays.asList(2, 3, 6, 7);
         redLeft = Arrays.asList(0, 1, 6, 7);
         greenLeft = Arrays.asList(2, 3, 4, 5);
         yellowTopGreenLeft = Arrays.asList(2, 4);
         blueTopGreenLeft = Arrays.asList(3, 5);
         yellowTopRedLeft = Arrays.asList(0, 6);
         blueTopRedLeft = Arrays.asList(1, 7);
      }
      else if (numberOfTiles == 16)
      {
         yellowTop = Arrays.asList(0, 1, 2, 3, 12, 13, 14, 15);
         blueTop = Arrays.asList(4, 5, 6, 7, 8, 9, 10, 11);
         greenRight = Arrays.asList(1, 2, 5, 6, 9, 10, 13, 14);
         redRight = Arrays.asList(0, 3, 4, 7, 8, 11, 12, 15);
         blueBottom = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
         yellowBottom = Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15);
         redLeft = Arrays.asList(0, 1, 4, 5, 8, 9, 12, 13);
         greenLeft = Arrays.asList(2, 3, 6, 7, 10, 11, 14, 15);
         yellowTopGreenLeft = Arrays.asList(2, 3, 14, 15);
         blueTopGreenLeft = Arrays.asList(6, 7, 10, 11);
         yellowTopRedLeft = Arrays.asList(0, 1, 12, 13);
         blueTopRedLeft = Arrays.asList(4, 5, 8, 9);
      }
      else
      {
         handleException(new IllegalArgumentException());
      }
      return Arrays.asList(yellowTop, blueTop, greenRight, redRight,
                           blueBottom, yellowBottom, redLeft, greenLeft,
                           yellowTopGreenLeft, blueTopGreenLeft, yellowTopRedLeft, blueTopRedLeft);
   }


   /**
    * Helper Methode to convert an index to the respectiv color.
    */
   private Color toColor(int idx)
   {
      switch (idx)
      {
         case 0:
            return new Color(196, 163, 0); // Dark Yellow
         case 1:
            return new Color(0, 127, 14); // Dark Green
         case 2:
            return new Color(0, 74, 127); // Steel Blue
         case 3:
            return new Color(120, 0, 0); // Dark Red
      }
      return null;
   }
}
