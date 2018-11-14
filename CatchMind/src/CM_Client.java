import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javafx.embed.swing.*;
import javafx.scene.media.*;

public class CM_Client extends CM_Client_GUI implements ActionListener
{
	Canvas canvas = new Brush();
	CanvasHandler ch = new CanvasHandler();
	Color color;
	Graphics g;
	Graphics2D g2d;
	MediaPlayer p;
	
	int port = 7777;
	String playerName, playerScore, playerIdx; // Ŭ���̾�Ʈ �̸�, ����, �ε��� ����
	boolean gameStart, auth; // ���� ���� ���� & ������ ���� üũ 
	
	public CM_Client(){
		init();
	}
	
	void init(){
		String nickName = CM_Login.nickName;
		String ip = CM_Login.ip;
		try{
			Socket s = new Socket(ip, port);
			Sender sender = new Sender(s, nickName);
			Listener listener = new Listener(s);
			new Thread(sender).start();
			new Thread(listener).start();
			
			// �̺�Ʈ ������ �߰� (ä�� & ���� & ĵ���� ��Ʈ��)
			textField.addKeyListener(new Sender(s, nickName));
			canvas.setBackground(Color.WHITE);
			panel_Canvas.add(canvas, BorderLayout.CENTER);
			 canvas.addMouseMotionListener(new Sender(s, nickName)); canvas.addMouseMotionListener(ch);
			btn_Color1.addActionListener(new Sender(s, nickName)); btn_Color1.addActionListener(ch);
			btn_Color2.addActionListener(new Sender(s, nickName)); btn_Color2.addActionListener(ch);
			btn_Color3.addActionListener(new Sender(s, nickName)); btn_Color3.addActionListener(ch);
			btn_Color4.addActionListener(new Sender(s, nickName)); btn_Color4.addActionListener(ch);
			btn_Color5.addActionListener(new Sender(s, nickName)); btn_Color5.addActionListener(ch);
			btn_Erase.addActionListener(new Sender(s, nickName)); btn_Erase.addActionListener(ch);
			btn_EraseAll.addActionListener(new Sender(s, nickName)); btn_EraseAll.addActionListener(ch);
			btn_GG.addActionListener(new Sender(s, nickName));
			btn_Ready.addActionListener(new Sender(s, nickName));
			btn_Exit.addActionListener(this);
		}catch(UnknownHostException uh){
			JOptionPane.showMessageDialog(null, "ȣ��Ʈ�� ã�� �� �����ϴ�!", "ERROR", JOptionPane.WARNING_MESSAGE);
		}catch(IOException io){
			JOptionPane.showMessageDialog(null, "���� ���� ����!\n������ ���� �ִ� �� �����ϴ�.", "ERROR", JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
	}
	
	public void actionPerformed(ActionEvent e){ // ���� ��ư �׼� �̺�Ʈ ó��
		if(e.getSource() == btn_Exit){
			int select = JOptionPane.showConfirmDialog(null, "���� ������ �����Ͻðڽ��ϱ�?", "Exit", JOptionPane.OK_CANCEL_OPTION);
			if(select == JOptionPane.YES_OPTION) System.exit(0);
		}
	}
	
	// ���� Ŭ���� - �۽�
	class Sender extends Thread implements KeyListener, ActionListener, MouseMotionListener
	{
		DataOutputStream dos;
		Socket s;
		String nickName;

		Sender(Socket s, String nickName){
			this.s = s;
			try{
				dos = new DataOutputStream(this.s.getOutputStream());
				this.nickName = nickName;
			}catch(IOException io){}
		}

		public void run(){
			try{
				dos.writeUTF(nickName);
			}catch(IOException io){}
		}
		
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == btn_Ready){ // '�غ�' ��ư
				try{
					dos.writeUTF(CM_ENUM.CHAT + "[ " + nickName + " �� �غ� �Ϸ� ! ]");
					dos.flush();
					dos.writeUTF(CM_ENUM.READY);
					dos.flush();
					btn_Ready.setEnabled(false);
				}catch(IOException io){}
			}else if(e.getSource() == btn_Color1 && auth == true){ // ���� ���� ��ư
				try{
					dos.writeUTF(CM_ENUM.CHANGE_COLOR + "Red");
					dos.flush();
				}catch(IOException io){}
			}else if(e.getSource() == btn_Color2 && auth == true){
				try{
					dos.writeUTF(CM_ENUM.CHANGE_COLOR + "Green");
					dos.flush();
				}catch(IOException io){}
			}else if(e.getSource() == btn_Color3 && auth == true){
				try{
					dos.writeUTF(CM_ENUM.CHANGE_COLOR + "Blue");
					dos.flush();
				}catch(IOException io){}
			}else if(e.getSource() == btn_Color4 && auth == true){
				try{
					dos.writeUTF(CM_ENUM.CHANGE_COLOR + "Yellow");
					dos.flush();
				}catch(IOException io){}
			}else if(e.getSource() == btn_Color5 && auth == true){
				try{
					dos.writeUTF(CM_ENUM.CHANGE_COLOR + "Black");
					dos.flush();
				}catch(IOException io){}
			}else if(e.getSource() == btn_Erase && auth == true){ // '�����' ��ư
				try{
					dos.writeUTF(CM_ENUM.ERASE);
					dos.flush();
				}catch(IOException io){}
			}else if(e.getSource() == btn_EraseAll && auth == true){ // '��� �����' ��ư
				try{
					if(auth == true){
						dos.writeUTF(CM_ENUM.ERASE_ALL);
						dos.flush();
					}
				}catch(IOException io){}
			}else if(e.getSource() == btn_GG && auth == true){ // '����' ��ư
				try{
					if(auth == true){
						dos.writeUTF(CM_ENUM.GG);
						dos.flush();
					}
				}catch(IOException io){}
			}
		}
		
		public void keyReleased(KeyEvent e){ // ä�� �Է�
			if(e.getKeyCode() == KeyEvent.VK_ENTER){
				String chat = textField.getText();
				textField.setText("");
				try{
					dos.writeUTF(CM_ENUM.CHAT + nickName + " : " + chat);
					dos.flush();
				}catch(IOException io){}
			}
		}
		public void keyTyped(KeyEvent e){}
		public void keyPressed(KeyEvent e){}
		
		public void mouseDragged(MouseEvent e){ // ���콺 ��ǥ ����
		    try{
		    	if(auth == true){
		    		int x = e.getX(); int y = e.getY();
		    		dos.writeUTF(CM_ENUM.MOUSE_XY + x + "." + y);
		    		dos.flush();
		    	}
		    }catch(IOException io){}
		}
		public void mousePressed(MouseEvent e){}
		public void mouseMoved(MouseEvent e){}
	}

	// ���� Ŭ���� - ����
	class Listener extends Thread
	{
		Socket s;
		DataInputStream dis;

		Listener(Socket s){
			this.s = s;
			try{
				dis = new DataInputStream(this.s.getInputStream());
			}catch(IOException io){}
		}

		public void run(){
			while(dis != null){
				try{
					String msg = dis.readUTF();
					if(msg.startsWith(CM_ENUM.UPD_CLIST)){ // ��ɾ� : Ŭ���̾�Ʈ ��� ����
						playerName = msg.substring(7, msg.indexOf(" "));
						playerScore = msg.substring(msg.indexOf(" ") + 1, msg.indexOf("#"));
						playerIdx = msg.substring(msg.indexOf("#") + 1);
						updateClientList(); // Ŭ���̾�Ʈ ��� ����
					}else if(msg.startsWith(CM_ENUM.START)){ // ��ɾ� : ���� ���� ( + Ÿ�̸�)
						gameStart = true;
						g = canvas.getGraphics(); // ĵ���� ���� �ʱ�ȭ
						g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
						Brush canvas2 = (Brush)canvas;
						canvas2.color = Color.BLACK;
						color = Color.BLACK;
						bgm(CM_ENUM.BGM_PLAY); // BGM ���
					}else if(msg.equals(CM_ENUM.GG)){ // ��ɾ� : ������ ���� ����
						gameStart = false;
						auth = false;
						textField.setEnabled(true);
						btn_Ready.setEnabled(true);
						bgm(CM_ENUM.BGM_STOP); // BGM ����
					}else if(msg.equals(CM_ENUM.END)){ // ��ɾ� : ���� ����
						gameStart = false;
						auth = false;
						textField.setEnabled(true);
						btn_Ready.setEnabled(true);
						label_Timer.setText("00 : 00");
						bgm(CM_ENUM.BGM_STOP); // BGM ����
					}else if(msg.startsWith(CM_ENUM.EXAM)){ // ��ɾ� : ���� ���� ����
						if(auth == true){
							label_Exam_Sub.setText(msg.substring(7));
						}else{
							label_Exam_Sub.setText(" ??? ");
						}
					}else if(msg.startsWith(CM_ENUM.AUTH)){ // ��ɾ� : ������ ���� �ο�
						if(CM_Login.nickName.equals(msg.substring(7))){
							auth = true;
							textArea.append("\n[ ����� ���� �������Դϴ� !! ]" + "\n\n");
							textField.setEnabled(false);
						}
					}else if(msg.startsWith(CM_ENUM.MOUSE_XY)){ // ��ɾ� : ĵ���� ���� (���콺 ��ǥ ����)
						if(auth == false){
							int tempX = Integer.parseInt(msg.substring(7, msg.indexOf("."))); 
							int tempY = Integer.parseInt(msg.substring(msg.indexOf(".") + 1));
							g = canvas.getGraphics();
							g2d = (Graphics2D)g;
							g2d.setColor(color);
				            g2d.setStroke(new BasicStroke(6));
				            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		                    g.drawLine(tempX, tempY, tempX, tempY);
						}
					}else if(msg.startsWith(CM_ENUM.TIMER)){ // ��ɾ� : Ÿ�̸� �ð� ǥ��
						label_Timer.setText(msg.substring(7));
					}else if(msg.startsWith(CM_ENUM.CHANGE_COLOR)){ // ��ɾ� : �÷� ����
						String temp = msg.substring(7);
						switch(temp){
							case "Red": color = Color.RED; break;
							case "Green": color = Color.GREEN; break;
							case "Blue": color = Color.BLUE; break;
							case "Yellow": color = Color.YELLOW; break;
							case "Black": color = Color.BLACK; break;
						}
					}else if(msg.equals(CM_ENUM.ERASE)){ // ��ɾ� : �����
						color = Color.WHITE;
					}else if(msg.equals(CM_ENUM.ERASE_ALL)){ // ��ɾ� : ��� �����
						g = canvas.getGraphics();
						g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
					}else{ // �Ϲ� ä�� ���
						textArea.append(msg + "\n");
						scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
					}
				}catch(IOException io){
					textArea.append("[ �������� ������ ���������ϴ�. �г��� �ߺ�, ���� ���� �ʰ�, ���� �������� ��� ������ �źε˴ϴ�. ]\n[ 3�� �� ���α׷��� �����մϴ� .. ]");
					try{
						Thread.sleep(3000);
						System.exit(0);
					}catch(InterruptedException it){}
				}
			}
		}
		
		public void updateClientList(){ // Ŭ���̾�Ʈ ��� �߰�
			ImageIcon ii;
			if(Integer.parseInt(playerIdx) == 0){
				ii = new ImageIcon("image\\p1.png");
				ii.getImage().flush();
				label_Client1.setIcon(ii);
				label_Client1_Sub.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				deleteClientList();
			}else if(Integer.parseInt(playerIdx) == 1){
				ii = new ImageIcon("image\\p2.png");
				ii.getImage().flush();
				label_Client2.setIcon(ii);
				label_Client2_Sub.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				deleteClientList();
			}else if(Integer.parseInt(playerIdx) == 2){
				ii = new ImageIcon("image\\p3.png");
				ii.getImage().flush();
				label_Client3.setIcon(ii);
				label_Client3_Sub.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				deleteClientList();
			}else if(Integer.parseInt(playerIdx) == 3){
				ii = new ImageIcon("image\\p4.png");
				ii.getImage().flush();
				label_Client4.setIcon(ii);
				label_Client4_Sub.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				deleteClientList();
			}
		}
		
		public void deleteClientList(){ // Ŭ���̾�Ʈ ��� ����
			ImageIcon ii2;
			ii2 = new ImageIcon("image\\p0.png");
			if(Integer.parseInt(playerIdx) == 0){
				label_Client2.setIcon(ii2);
				label_Client2_Sub.setText("[ �г��� / ���� ]");
				label_Client3.setIcon(ii2);
				label_Client3_Sub.setText("[ �г��� / ���� ]");
				label_Client4.setIcon(ii2);
				label_Client4_Sub.setText("[ �г��� / ���� ]");
			}else if(Integer.parseInt(playerIdx) == 1){
				label_Client3.setIcon(ii2);
				label_Client3_Sub.setText("[ �г��� / ���� ]");
				label_Client4.setIcon(ii2);
				label_Client4_Sub.setText("[ �г��� / ���� ]");
			}else if(Integer.parseInt(playerIdx) == 2){
				label_Client4.setIcon(ii2);
				label_Client4_Sub.setText("[ �г��� / ���� ]");
			}
		}
		
		void bgm(String play){ // BGM ��� & ����
			try{
				if(play.equals(CM_ENUM.BGM_PLAY)){
					new JFXPanel();
					File f = new File("bgm\\bgm.mp3");
					Media bgm = new Media(f.toURI().toURL().toString());
			        p = new MediaPlayer(bgm);
					p.play();
				}else if(play.equals(CM_ENUM.BGM_STOP)){
					p.stop();
					p.setMute(true);
					p.dispose();
				}
			}catch(Exception e){}
		}
	}

	// ���� Ŭ���� - ĵ���� �ڵ鷯
	class CanvasHandler extends JFrame implements ActionListener, MouseMotionListener
	{	
		int x1, x2, y1, y2;
		public void mouseDragged(MouseEvent e){
		    x1 = e.getX(); y1 = e.getY();
		    ((Brush)canvas).x1 = x1; ((Brush)canvas).y1 = y1;
		    canvas.repaint();
		}
		public void mousePressed(MouseEvent e){}
		public void mouseMoved(MouseEvent e){}
		
		public void actionPerformed(ActionEvent e){
			Object obj = e.getSource();
			Brush canvas2 = (Brush)canvas;
		   
			if(auth == true){ // ������ ������ ���� ���¿��߸� ĵ���� ���� ����
			    if(obj == btn_Color1){
				    canvas2.color = Color.RED;
			    }else if(obj == btn_Color2){
			    	canvas2.color = Color.GREEN;
			    }else if(obj == btn_Color3){
			    	canvas2.color = Color.BLUE;
			    }else if(obj == btn_Color4){
			    	canvas2.color = Color.YELLOW;
			    }else if(obj == btn_Color5){
			    	canvas2.color = Color.BLACK;
			    }else if(obj == btn_Erase){
			    	canvas2.color = canvas.getBackground();
			    }else if(obj == btn_EraseAll){
			    	Graphics g = canvas2.getGraphics();
				    g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); 
			    }
			}
		}
	}
	
	// ���� Ŭ���� - ĵ���� �귯�� ����
	class Brush extends Canvas
	{
		int x1, x2;
		int y1, y2;
		Color color = Color.BLACK;

		void paintComponent(Graphics g){
			if(gameStart == true && auth == true){ // ������ ���۵Ǿ���, ������ ������ ���� ���¿��� �׸��� ����
				Graphics2D g2d = (Graphics2D)g;
	            g2d.setColor(color);
	            g2d.setStroke(new BasicStroke(6));
	            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            g2d.drawLine(x1, y1, x1, y1);
			}
		}
		
		public void update(Graphics g){
			paintComponent(g);
		}
	}
}