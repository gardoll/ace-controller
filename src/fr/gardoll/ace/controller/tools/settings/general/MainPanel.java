package fr.gardoll.ace.controller.tools.settings.general ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.Log ;
import fr.gardoll.ace.controller.core.Utils ;
import fr.gardoll.ace.controller.settings.GeneralSettings ;
import fr.gardoll.ace.controller.settings.ParametresSession ;
import fr.gardoll.ace.controller.ui.UiUtils ;

public class MainPanel extends javax.swing.JPanel
{
  private static final Logger _LOG = Log.HIGH_LEVEL;
  
  private static final long serialVersionUID = 2498266605755338610L ;

  private final List<Panel> _panels = new ArrayList<Panel>() ;

  /**
   * Creates new form MainPanel
   */
  public MainPanel()
  {
    initComponents() ;
    initCustom() ;
  }

  private void initCustom()
  {
    PumpPanel generalPanel = new PumpPanel() ;
    this.settingsTabbedPanel.add(generalPanel, "Pump") ;
    this._panels.add(generalPanel) ;
    
    CarouselPanel carouselPanel = new CarouselPanel();
    this.settingsTabbedPanel.add(carouselPanel, "Carousel") ;
    this._panels.add(carouselPanel) ;
    
    MiscellaneousPanel miscPanel = new MiscellaneousPanel();
    this.settingsTabbedPanel.add(miscPanel, "Misc") ;
    this._panels.add(miscPanel) ;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints ;

    settingsTabbedPanel = new javax.swing.JTabbedPane() ;
    controlPanel = new javax.swing.JPanel() ;
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
        new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767)) ;
    okButton = new javax.swing.JButton() ;
    cancelButton = new javax.swing.JButton() ;

    setPreferredSize(new java.awt.Dimension(780, 460)) ;
    setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(settingsTabbedPanel, gridBagConstraints) ;

    controlPanel.setLayout(new java.awt.GridBagLayout()) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    controlPanel.add(filler1, gridBagConstraints) ;

    okButton.setText("ok") ;
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        okButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 1 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    controlPanel.add(okButton, gridBagConstraints) ;

    cancelButton.setText("cancel") ;
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt) ;
      }
    }) ;
    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 2 ;
    gridBagConstraints.gridy = 0 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST ;
    gridBagConstraints.weightx = 1.0 ;
    gridBagConstraints.weighty = 1.0 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    controlPanel.add(cancelButton, gridBagConstraints) ;

    gridBagConstraints = new java.awt.GridBagConstraints() ;
    gridBagConstraints.gridx = 0 ;
    gridBagConstraints.gridy = 1 ;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH ;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST ;
    gridBagConstraints.weightx = 0.5 ;
    gridBagConstraints.weighty = 0.5 ;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2) ;
    add(controlPanel, gridBagConstraints) ;
  }// </editor-fold>

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    boolean succeeded = true;
    
    for(Panel panel: this._panels)
    {
      try
      {
        _LOG.info(String.format("checking %s settings", panel.getName()));
        panel.check();
      }
      catch (Exception e)
      {
        String msg = String.format("error in %s settings", panel.getName());
        _LOG.error(msg, e);
        Utils.reportError(msg, e);
        succeeded = false;
      }
    }
    
    if(succeeded)
    {
      for(Panel panel: this._panels)
      {
        try
        {
          _LOG.info(String.format("altering the %s settings", panel.getName()));
          panel.set();
        }
        catch (Exception e)
        {
          String msg = String.format("error in %s settings", panel.getName());
          _LOG.error(msg, e);
          Utils.reportError(msg, e);
          succeeded = false;
        }
      }
      
      try
      {
        _LOG.info("saving general settings");
        GeneralSettings.instance().save();
      }
      catch (Exception e)
      {
        String msg = "error while saving general settings";
        _LOG.error(msg, e);
        Utils.reportError(msg, e);
        return;
      }
      
      ParametresSession.getInstance().reset();
      
      UiUtils.getParentDialog(this).dispose();
    }
  }

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    UiUtils.getParentDialog(this).dispose();
  }

  // Variables declaration - do not modify
  private javax.swing.JButton cancelButton ;
  private javax.swing.JPanel controlPanel ;
  private javax.swing.Box.Filler filler1 ;
  private javax.swing.JButton okButton ;
  private javax.swing.JTabbedPane settingsTabbedPanel ;
  // End of variables declaration
}
