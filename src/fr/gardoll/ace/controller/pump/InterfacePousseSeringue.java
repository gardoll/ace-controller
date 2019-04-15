package fr.gardoll.ace.controller.pump;

import java.io.Closeable;
import java.math.RoundingMode ;
import java.text.DecimalFormat ;
import java.text.DecimalFormatSymbols ;
import java.util.Locale ;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.comm.FlowControl ;
import fr.gardoll.ace.controller.comm.Parity ;
import fr.gardoll.ace.controller.comm.SerialCom;
import fr.gardoll.ace.controller.comm.StopBit;
import fr.gardoll.ace.controller.common.ConfigurationException ;
import fr.gardoll.ace.controller.common.InitializationException ;
import fr.gardoll.ace.controller.common.Naming ;
import fr.gardoll.ace.controller.common.SerialComException ;

public class InterfacePousseSeringue  implements Closeable
{
  //caractéristique du pousse seringue en m/min
  public final static double COURCE_LINEAIRE_MAX = 0.1269 ;
  //diamètre de seringue maximum pour le pousse seringue en mm
  public final static int DIAMETRE_MAX = 140 ;
  
  public final static DecimalFormatSymbols DECIMAL_SYMBOLS =
      new DecimalFormatSymbols(Locale.US);
  
  private static final Logger _LOG = LogManager.getLogger(InterfacePousseSeringue.class.getName());
  
  private final static DecimalFormat[] _DOUBLE_FORMATTERS = new DecimalFormat[4];
  
  private final SerialCom _port;
  
  // dépendant uniquement du diametre du type de seringue utilisé.
  private final int _debitMaxIntrinseque;
  
  static
  {
    String[] formats = {"#.###", "##.##", "###.#", "####"} ;
        
    for(int index=0 ; index < _DOUBLE_FORMATTERS.length ; index++)
    {
      _DOUBLE_FORMATTERS[index] = new DecimalFormat(formats[index]);
      _DOUBLE_FORMATTERS[index].setRoundingMode(RoundingMode.FLOOR);
      _DOUBLE_FORMATTERS[index].setDecimalSeparatorAlwaysShown(false);
      _DOUBLE_FORMATTERS[index].setDecimalFormatSymbols(DECIMAL_SYMBOLS);
    }
  }
  
  // requires 0 < diametreSeringue <= DIAMETRE_MAX
  public InterfacePousseSeringue(double diametreSeringue, SerialCom port)
      throws InitializationException
  {
    _LOG.info("initializing the pump interface");
    
    this._port = port ;
    
    // Initializing the serial port.
    try
    {
      this._port.setVitesse(9600) ;
      this._port.setByteSize (8);
      this._port.setStopBit(StopBit.ONESTOPBIT);
      this._port.setParite(Parity.NOPARITY);
      this._port.setControlFlux(FlowControl.XON_XOFF);
      this._port.setTimeOut(300) ;
    }
    catch(SerialComException e)
    {
      String msg = String.format("error while initializing the pump serial port: %s",
          e.getMessage());
      _LOG.fatal(msg);
      throw new InitializationException(msg, e);
    }
 
    try
    {
      // contient le code de vérification.
      this._debitMaxIntrinseque = InterfacePousseSeringue.debitMaxIntrinseque(diametreSeringue);

      this.dia(diametreSeringue);
    }
    catch(SerialComException e)
    {
      String msg = "error while initializing the pump";
      _LOG.fatal(msg);
      throw new InitializationException(msg, e);
    }
  }

  //traitement de la réponse de l'interface en cas d'erreur => exception.
  private void traitementReponse(String message) throws SerialComException
  {
    if (message == "EE")
    {
      String msg = "pump serial communication failure" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }

    else if (message == "NA::")
    {
      String msg = "unknown pump order" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }

    else if (message == "")
    {
      String msg = "the pump did not acknowledgement the order" ;
      _LOG.error(msg);
      throw new SerialComException(msg) ;
    }
  }
  
  //renvoie la réponse de l'interface
  private String traitementOrdre(String ordre) throws SerialComException
  {
    this._port.ecrire(ordre) ;

    String reponse = this.lectureReponse() ;

    this.traitementReponse(reponse) ;

    return reponse  ;
  }
  
  private String lectureReponse() throws SerialComException
  {
    return this._port.lire();
  }
  
  // transforme float en string mais
  // où , est transformée en .  (séparateur des réels)
  // et format le nombre pour qu'il n'y ait que 4 chiffres au plus.
  private String formatage(double nombre)
  {
    // le pousse seringue n'acceptant que des nombres à 4 chiffres au plus ,
    // sans compter le séparateur décimal
    // qui doit être un point, le paramètre nombre doit être formaté en conséquence.
    
    int truncatedValue = (int)nombre;
    int index = 0;
    
    if (truncatedValue < 10)
    {
      index = 0;
    }
    else if(truncatedValue < 100)
    {
      index = 1;
    }
    else if(truncatedValue < 1000)
    {
      index = 2;
    }
    else if(truncatedValue < 10000)
    {
      index = 3; 
    }
    else
    {
      String msg = String.format("unsupported format number '%s'", nombre);
      _LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
    
    return _DOUBLE_FORMATTERS[index].format(nombre);
  }
  
  //reprise ou démarrage
  public void run() throws SerialComException
  {
    this.traitementOrdre( "run\r" );
  }
  
  // pause
  public void stop() throws SerialComException
  {
    this.traitementOrdre ("stop\r");
  }
  
  // Don't throw any exception as it is an emergency method.
  // précondition : le threadSequence doit être détruit ( pthread_cancel ) ou inexistant
  // précondition non vérifiée !!!
  public void arretUrgence()
  {
    //précondition : le threadSequence doit être détruit ( pthread_cancel ) ou inexistant
    try
    {
      this._port.ecrire("stop\r") ;
      String reponse = this.lectureReponse() ;
      this.traitementReponse(reponse) ;
    }
    catch (SerialComException e)
    {
      String msg = String.format("error while emergency stopping: %s", e.getMessage()) ;
      _LOG.error(msg);
    }
  }
  
  // en mm requires diametre > 0
  public void dia(double diametre) throws SerialComException
  {
    String ordre = String.format("dia %s\r", formatage(diametre)) ;
    this.traitementOrdre (ordre);
  }
  
  public boolean running() throws SerialComException
  {
    boolean result = this.traitementOrdre("run?\r") != "::" ;
    return result;
  }
  
  // en mL
  public double deliver() throws SerialComException
  {
    char[] message_brute = this.traitementOrdre("del?\r").toCharArray() ;

    // Attention ne revoie un réel que si le volume à délivré en est un.
    // Ainsi : 1. ou 1.0 ne donnera pas de réponse en réel donc la réponse sera
    // 0 puis 1 à la fin !!!! Il n'y a donc aucun intérêt.
    // Parade : passer en micro litre quand < 10 mL.
    
    StringBuilder sb = new StringBuilder(); 
    boolean micro_litre = false ;

    for (int i = 1 ; i <= message_brute.length ; i++)
    {
      if (Character.isDigit(message_brute[i]))
      {
        sb.append(message_brute[i]) ;
      }

      if (message_brute[i] == DECIMAL_SYMBOLS.getDecimalSeparator())
      {
        sb.append(Naming.DECIMAL_SYMBOLS.getDecimalSeparator()) ;
      }

      if (message_brute[i] == 'u')
      {
        micro_litre = true ;
      }
    }

    double raw_value = Double.valueOf(sb.toString());
    
    if (micro_litre)
    {
      return (raw_value/1000.) ;
    }
    else
    {
      return raw_value ;
    }
  }
  
  // en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratei(double debit) throws SerialComException
  {
    if (debit <= 0.)
    {
      String msg = String.format("the value of the rate '%s' cannot be negative or null",
                                 debit);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }
    else if (debit > this._debitMaxIntrinseque)
    {
      String msg = String.format("the value of the rate '%s' cannot be greater than %s mL/min",
                                 debit, this._debitMaxIntrinseque);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }
    
    String ordre = String.format("ratei %s ml/m\r", formatage(debit)) ;
    this.traitementOrdre(ordre) ;
  }
  
  //en mL/min   requires 0 < debit <= _debitMaxIntrinseque
  public void ratew(double debit) throws SerialComException
  {
    if (debit <= 0.)
    {
      String msg = String.format(
          "the value of the rate '%s' cannot be negative or null", debit) ;
      _LOG.fatal(msg) ;
      throw new RuntimeException(msg) ;
    }
    else if (debit > this._debitMaxIntrinseque)
    {
      String msg = String.format(
          "the value of the rate '%s' cannot be greater than %s mL/min", debit,
          this._debitMaxIntrinseque) ;
      _LOG.fatal(msg) ;
      throw new RuntimeException(msg) ;
    }

    String ordre = String.format("ratew %s ml/m\r", formatage(debit));
    this.traitementOrdre(ordre) ;
  }
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void voli(double volume) throws SerialComException
  {
    if (volume <= 0.)
    {
      String msg = String.format("the value of the volume '%s' cannot be negative or null",
                                 volume);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }

    String ordre = null;

    if (volume < 10.)
    {
      // permet le suivie du volume délivré si <1 ml voir la fonction deliver.
      ordre = String.format("voli %s ul\r", formatage((volume * 1000))) ;
    }
    else
    {
      ordre = String.format("voli %s ml\r", formatage(volume));
    }

    this.traitementOrdre(ordre) ;
  }
  
  // en mL seulement 4 caractères sans compter la virgule.
  // requires volume > 0
  public void volw(double volume) throws SerialComException
  {
    if (volume <= 0.)
    {
      String msg = String.format("the value of the volume '%s' cannot be negative or null",
                                 volume);
      _LOG.fatal(msg);
      throw new RuntimeException(msg) ;
    }

    String ordre = null;

    if (volume < 10.)
    {
      // permet le suivie du volume délivré si <1 ml voir la fonction deliver.
      ordre = String.format("volw %s ul\r", formatage(volume * 1000));
    }
    else
    {
      ordre = String.format("volw %s ml\r", formatage(volume));
    }

    this.traitementOrdre(ordre) ;
  }
  
  public void modeI() throws SerialComException
  {
    this.traitementOrdre("mode i\r") ; 
  }
  
  public void modeW() throws SerialComException
  {
    this.traitementOrdre("mode w\r") ;
  }
  
  public static int debitMaxIntrinseque(double diametreSeringue)
  {
    if (diametreSeringue <= 0)
    {
      String msg = String.format("the value of the syringe diameter '%s' cannot be negative or null",
                                 diametreSeringue) ;
      _LOG.fatal(msg);
      throw new ConfigurationException(msg);
    }
    else if (diametreSeringue > DIAMETRE_MAX)
    {
      String msg = String.format("the value of the syringe diameter '%s' cannot be greater than %s",
                                 diametreSeringue, DIAMETRE_MAX);
      _LOG.fatal(msg);
      throw new ConfigurationException (msg);
    }
    
    double result = Math.pow(diametreSeringue/2. , 2.) * Math.PI * COURCE_LINEAIRE_MAX  ;
    // arrondi par à l'entier inférieur à cause spec du pousse seringue .
    return (int) (result) ;
  }
  
  @Override
  public void close()
  {
    this._port.close() ;
  }
  
  public static void main(String[] args)
  {
    
  }
}
