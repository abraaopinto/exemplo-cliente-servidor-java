import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Servidor extends JFrame{
   JTextArea jtaSaidaTexto;
   private ObjectOutputStream oosSaidaDados;
   private ObjectInputStream oisEntradaDados;
   ObjectInputStream acesso_vindo_cliente[];
   ObjectOutputStream acesso_indo_cliente[];
   private ServerSocket ssServidor;
   private Socket sConexao;
   private int vContador = 1;
   private Socket vCliente;
   private static int vIdCliente=0;
   String Clientes[];
   public Servidor()
   {
      super( "Servidor" );
	  Clientes = new String[10];
	  acesso_vindo_cliente = new ObjectInputStream[10];
	  acesso_indo_cliente = new ObjectOutputStream[10];
      Container container = getContentPane();

      jtaSaidaTexto = new JTextArea();
      jtaSaidaTexto.setEditable(false);
      jtaSaidaTexto.setForeground(new Color(200,100,100));
      container.add( new JScrollPane( jtaSaidaTexto ),
         BorderLayout.CENTER );

      setSize( 300, 150 );
      setVisible( true );
   }

   public void runServidor()
   {

      try {

         // Step 1: Create a ServerSocket.
         ssServidor = new ServerSocket( 5000 );
		//	run();
      	while ( true ) {

            // Step 2: Wait for a connection.
            vCliente = esperarPorConexao();

            // Step 3: Get input and output streams.
            obterFluxos(vCliente);
 
            // Step 4: Process connection.
           // processarConexao();

            // Step 5: Close connection.
            //encerrarConexao();
            
         }
      }

      catch ( EOFException eofException ) {
         System.out.println( "Cliente encerrou a conexão" );
      }

      catch ( IOException ioException ) {
         ioException.printStackTrace();
      }
   }


   private Socket esperarPorConexao() throws IOException
   {
      jtaSaidaTexto.append( "[Esperando por conexão]\n" );

      sConexao = ssServidor.accept();
            
      jtaSaidaTexto.append( "Conexão nº[ " + vContador +
         "] recebida de: [ " +
         sConexao.getInetAddress().getHostName()+" ]" );
      return sConexao;
   }

   private void obterFluxos(Socket pCliente) throws IOException
   {
 
      oosSaidaDados = new ObjectOutputStream(pCliente.getOutputStream() );

      oosSaidaDados.flush();

      oisEntradaDados = new ObjectInputStream(pCliente.getInputStream() );
	  
	  acesso_vindo_cliente[vIdCliente]=oisEntradaDados;
	  acesso_indo_cliente[vIdCliente]=oosSaidaDados;
	  Clientes[vIdCliente]=processarConexao(oisEntradaDados);
	  
	  (new recebeClientes(this,vIdCliente)).start();//classe interna que processa as conexões
		vIdCliente=vIdCliente+1;
		try{
		   Thread.sleep(3000);
		}
		catch (InterruptedException e){}
      jtaSaidaTexto.append( "\n[Adiquirido fluxos de I/O]\n" );
      
      
   }

   String processarConexao(ObjectInputStream pOISEntradaDados) throws IOException
   {
     String message="";
        try {
            message = ( String ) pOISEntradaDados.readObject();
            jtaSaidaTexto.append( "\n[Servidor] <--< [" + message+"]" );
            jtaSaidaTexto.setCaretPosition(
               jtaSaidaTexto.getText().length() );
         }
         catch ( ClassNotFoundException classNotFoundException ) {
            jtaSaidaTexto.append( "\n[Tipo de mensagem desconhecida]" );
         }
	return message;
   }

   void encerrarConexao() throws IOException
   {
      jtaSaidaTexto.append( "\n[Usuario terminou a conexão]" );
      oosSaidaDados.close();
      oisEntradaDados.close();
      sConexao.close();
    }

   void enviarMensagem(ObjectOutputStream pOOSSaidaDados, String pMensagem )
   {
      try {
         pOOSSaidaDados.writeObject( pMensagem );
         pOOSSaidaDados.flush();
         jtaSaidaTexto.append( "\n[Servidor] >--> [" + pMensagem+"]" );
      }


      catch ( IOException ioException ) {
         jtaSaidaTexto.append( "\n[!]Erro ao enviar a mensagem" );
      }
   }

   public static void main( String args[] )
   {
      Servidor application = new Servidor();

      application.setDefaultCloseOperation(
         JFrame.EXIT_ON_CLOSE );

      application.runServidor();
   }

}
//classe interna que processa os clientes 
class recebeClientes extends Thread {
	Servidor objServidor;
	int index;
public recebeClientes(Servidor objServidor, int index){
	        this.objServidor = objServidor;
		    this.index = index;
}
	public void run(){
            setPriority(MAX_PRIORITY);
            objServidor.enviarMensagem(objServidor.acesso_indo_cliente[index],  "Cliente(" + objServidor.Clientes[index]
    	          + "): " + "Você está conectado ao Servidor");
			String temp="";
			
			do {
				try{
				
				temp = objServidor.processarConexao( objServidor.acesso_vindo_cliente[index] );
						
				objServidor.enviarMensagem(objServidor.acesso_indo_cliente[index],  "Cliente(" + objServidor.Clientes[index]
    	          + "): " + "ok mensagem recebida");
    	          
				}catch ( IOException ioException ) {
         		   objServidor.jtaSaidaTexto.append( "\n[Tipo de mensagem desconhecida]" );
         		}
			}while(!temp.equals( "sair" ));
    		//server.encerrarConexao();
    	    
	}

}