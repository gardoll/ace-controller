package fr.gardoll.ace.controller.valves;

import org.apache.logging.log4j.LogManager ;
import org.apache.logging.log4j.Logger ;

import fr.gardoll.ace.controller.com.ParaCom ;
import fr.gardoll.ace.controller.core.ParaComException ;

public class ParaComStub implements ParaCom
{
  private static final Logger _LOG = LogManager.getLogger(ParaComStub.class.getName());
  
  public ParaComStub()
  {
    _LOG.debug("instanciating paracom stub");
  }
  @Override
  public String getId()
  {
    _LOG.debug("stubbing method getId");
    return "stub";
  }

  @Override
  public void close()
  {
    _LOG.debug("stubbing close");
  }
  
  @Override
  public void send(byte[] order) throws ParaComException, InterruptedException
  {
    _LOG.debug("stubbing send");
  }
}
