package fr.gardoll.ace.controller.core;

import java.util.Collections ;
import java.util.HashSet ;
import java.util.Set ;

//import org.apache.logging.log4j.LogManager ;
//import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.autosampler.Passeur ;
import fr.gardoll.ace.controller.pump.PousseSeringue ;
import fr.gardoll.ace.controller.valves.Valves ;

public abstract class AbstractToolControl implements ToolControl
{
  // private static final Logger _LOG = LogManager.getLogger(AbstractToolControl.class.getName());
  
  final private Set<ControlPanel> _ctrlPanels = new HashSet<>();
  
  protected final PousseSeringue _pousseSeringue ;
  protected final Passeur _passeur ;
  protected final Valves _valves;
  
  protected final boolean _hasAutosampler;
  protected final boolean _hasPump;
  protected final boolean _hasValves ;
  
  public AbstractToolControl(ParametresSession parametresSession,
                             boolean hasPump, boolean hasAutosampler,
                             boolean hasValves)
                         throws InitializationException, InterruptedException
  {
    this._hasAutosampler = hasAutosampler;
    this._hasPump = hasPump;
    this._hasValves = hasValves;
    
    if(hasPump)
    {
      this._pousseSeringue = parametresSession.getPousseSeringue();
    }
    else
    {
      this._pousseSeringue = null;
    }
    
    if(hasAutosampler)
    {
      this._passeur = parametresSession.getPasseur();
    }
    else
    {
      this._passeur = null;
    }
    
    if(hasValves)
    {
      this._valves = parametresSession.getValves();
    }
    else
    {
      this._valves = null;
    }
  }
  
  @Override
  public void cancel()
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  // Thread must be terminated.
  public void reinit()
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void pause()
  {  
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void resume()
  {  
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Set<ControlPanel> getCtrlPanels()
  {
    return Collections.unmodifiableSet(this._ctrlPanels);
  }
  
  @Override
  public void addControlPanel(ControlPanel obs)
  {
    this._ctrlPanels.add(obs);
  }

  @Override
  public void removeControlPanel(ControlPanel obs)
  {
    this._ctrlPanels.remove(obs);
  }
  
  @Override
  public void notifyAction(Action action)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.majActionActuelle(action);
    }
  }
  
  @Override
  public void displayControlPanelModalMessage(String msg)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.displayModalMessage(msg);
    }
  }
  
  @Override
  public void notifyError(String msg, Throwable e)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.reportError(msg, e);
    }
  }
  
  @Override
  public void notifyError(String msg)
  {
    for(ControlPanel panel: this._ctrlPanels)
    {
      panel.reportError(msg);
    }
  }
  
  protected void handleException(String msg, Exception e)
  {
    this.notifyError(msg, e);
  }
}
