package fr.gardoll.ace.controller.com;

import java.nio.charset.Charset ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.core.InitializationException ;
import fr.gardoll.ace.controller.core.ParaComException ;
import fr.gardoll.ace.controller.core.SerialComException ;

// TODO add logging.
public class ArduinoParaCom implements ParaCom
{
  private static final int OPENING_DELAY = 1000;
  
  private static final int ATTENTE_EV = 200 ; // en milisecondes
  
  private static final Logger _LOG = LogManager.getLogger(ArduinoParaCom.class.getName());
  
  private final SerialCom _port;

  public ArduinoParaCom(SerialCom port) throws InitializationException, InterruptedException
  {
    _LOG.debug(String.format("initialiazing arduino with the port '%s'", port.getId()));
    this._port = port;
    
    try
    {
      this._port.setReadBufferSize(10);
      this._port.setMode(SerialMode.FULL_BLOCKING, SerialMode.FULL_BLOCKING);
      this._port.setCharset(Charset.forName("ASCII"));
      this._port.setVitesse(9600);
      this._port.setTimeOut(100);
      this._port.setByteSize(8);
      this._port.setParite(Parity.NOPARITY);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.open(ArduinoParaCom.OPENING_DELAY);
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while initializing the port '%s': %s",
          this._port.getPath(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new InitializationException(msg, e);
    }
  }
  
  @Override
  public String getId()
  {
    return this._port.getId();
  }

  @Override
  public void ouvrir(int numEv) throws ParaComException, InterruptedException
  {
    if (numEv < 0)
    {
      String msg = String.format("isolation valve number '%s' cannot be negative", numEv);
      _LOG.fatal(msg);
      throw new ParaComException(msg);
    }

    byte ordre ;

    switch (numEv)
    {
      case 0 : { ordre = 0 ; break ; }   //ttes ev fermées

      case 1 : { ordre = 1 ; break ; }  // uniquement l'ev 1 ouverte.

      case 2 : { ordre = 2 ; break ; }  // ...

      case 3 : { ordre = 4 ; break ; }

      case 4 : { ordre = 8 ; break ; }

      case 5 : { ordre = 16 ; break ; }

      case 6 : { ordre = 32 ; break ; }

      case 7 : { ordre = 64 ; break ; }

      default :
      {
        String msg = String.format("unsupported isolation valve number '%s'", numEv);
        _LOG.fatal(msg);
        throw new ParaComException(msg);
      }
    }
    
    try
    {
      byte b = Integer.valueOf(ordre).byteValue();
      this._port.write(new byte[] {b});
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while writing on port '%s': %s",
          this._port.getId(), e.getMessage());
      _LOG.fatal(msg, e);
      throw new ParaComException(msg, e);
    }
    
    this.check();
    Thread.sleep(ArduinoParaCom.ATTENTE_EV) ; //temps d'attente de l'exécution mécanique de l'ordre
  }
  
  private void check() throws ParaComException
  {
    try
    {
      String ack = this._port.lire().strip() ;
      if(ack.equals("E"))
      {
        String msg = "error while sending order to usb2valves";
        _LOG.fatal(msg);
        throw new ParaComException(msg);
      }
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while waiting the usb2valves acknowledge: %s",
          e.getMessage());
      _LOG.fatal(msg, e);
      throw new ParaComException(msg, e);
    }
  }

  @Override
  public void ouvrirH2O() throws ParaComException, InterruptedException
  {
    this.ouvrir(ParaCom.NUM_EV_H2O); 
  }

  @Override
  public void toutFermer() throws ParaComException, InterruptedException
  {
    this.ouvrir(ParaCom.NUM_SHUT_IV) ; 
  }

  @Override
  public void close()
  {
    _LOG.debug(String.format("closing the port '%s'", this._port.getId()));
    this._port.close();
  }
}
