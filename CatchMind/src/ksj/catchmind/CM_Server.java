package ksj.catchmind;

import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;

public class CM_Server extends CM_Server_GUI implements ActionListener
{
	ServerSocket ss;
	Socket s;
	int port = 7777;
	int readyPlayer; // ���� �غ�� Ŭ���̾�Ʈ ī��Ʈ
	int score;
	boolean gameStart; // ���� ���� ����
	String line;
	LinkedHashMap<String, DataOutputStream> clientList = new LinkedHashMap<String, DataOutputStream>(); // Ŭ���̾�Ʈ �̸�, ��Ʈ�� ����
	LinkedHashMap<String, Integer> clientInfo = new LinkedHashMap<String, Integer>(); // Ŭ���̾�Ʈ �̸�, ���� ����
	
	public CM_Server(){
		btn_ServerStart.addActionListener(this);
		btn_ServerClose.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e){ // '���� ���� & ����' ��ư �̺�Ʈ
		if(e.getSource() == btn_ServerStart){
			new Thread(){
				public void run(){
					try{
						Collections.synchronizedMap(clientList);
						ss = new ServerSocket(port);
						label_ServerStatus.setText("[ Server Started ]");
						textArea.append("[ ������ ���۵Ǿ����ϴ� ]" + "\n");
						btn_ServerStart.setEnabled(false);
						btn_ServerClose.setEnabled(true);
						while(true){
							s = ss.accept();
							if((clientList.size() + 1) > CM_ENUM.MAX_CLIENT || gameStart == true){ // ������ �ʰ��Ǿ��ų�, �������̶�� ���� ���� �ź�
								s.close();
							}else{
								Thread gm = new GameManager(s);
								gm.start();
							}
						}
					}catch(IOException io){
						System.exit(0);
					}
				}
			}.start();
		}else if(e.getSource() == btn_ServerClose){
			int select = JOptionPane.showConfirmDialog(null, "������ ���� �����Ͻðڽ��ϱ�?", "JAVA CatchMind Server", JOptionPane.OK_CANCEL_OPTION);
			try{
				if(select == JOptionPane.YES_OPTION){
					ss.close();
					label_ServerStatus.setText("[ Server Closed ]");
					textArea.append("[ ������ ����Ǿ����ϴ� ]" + "\n");
					btn_ServerStart.setEnabled(true);
					btn_ServerClose.setEnabled(false);
				}
			}catch(IOException io){
				io.printStackTrace();
			}
		}
	}
	
	public void sendSystemMsg(String msg){ // �ý��� �޽��� �� ��ɾ� �۽�
		Iterator<String> it = clientList.keySet().iterator();
		while(it.hasNext()){
			try{
				DataOutputStream dos = clientList.get(it.next());
				dos.writeUTF(msg);
				dos.flush();
			}catch(IOException io){
				io.printStackTrace();
			}
		}
	}

	// ���� Ŭ���� (���� ���� �� ����)
	public class GameManager extends Thread
	{
		Socket s;
		DataInputStream dis;
		DataOutputStream dos;
							
		public GameManager(Socket s){
			this.s = s;
			try{
				dis = new DataInputStream(this.s.getInputStream());
				dos = new DataOutputStream(this.s.getOutputStream());
			}catch(IOException io){
				io.printStackTrace();
			}
		}
		
		public void run(){
			String clientName = "";
			try{
				clientName = dis.readUTF();
				if(!clientList.containsKey(clientName)){ // �ߺ� �г��� ����
					clientList.put(clientName, dos);
					clientInfo.put(clientName, score);
				}else if(clientList.containsKey(clientName)){
					s.close(); // �г��� �ߺ���, ���� ���� �ź�
				}
				clientMgmt(clientName, "����");
				while(dis != null){
					String msg = dis.readUTF();
					filtering(msg); // ��ɾ� ���͸�
				}
			}catch(IOException io){
				clientList.remove(clientName); clientInfo.remove(clientName); // Ŭ���̾�Ʈ ����� ����
				closeAll();
				if(clientList.isEmpty() == true){ // ������ ���� Ŭ���̾�Ʈ�� �ϳ��� ���ٸ�, ���� �ݱ�
					try{
						ss.close();
						System.exit(0);
					}catch(IOException e){}
				}
				clientMgmt(clientName, "����");
				readyPlayer = 0; // ���ο� Ŭ���̾�Ʈ�� �����ص� ���� ���ۿ� ������ ������ ���� �ʱ�ȭ
				gameStart = false;
				sendSystemMsg(CM_ENUM.END); // Ŭ���̾�Ʈ �����, ��� ���� ����
			}
		}
		
		void clientMgmt(String clientName, String inout){
			sendSystemMsg("[ " + clientName + "���� " + inout + "�ϼ̽��ϴ�. ]\n(���� ������ �� : " + clientList.size() + "�� / 4��)");
			textArea.append("[ ���� ������ ��� (�� " + clientList.size() + "�� ������) ]\n");
			Iterator<String> it = clientList.keySet().iterator();
			while(it.hasNext()) textArea.append(it.next() + "\n");
			scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
			setClientInfo(); // Ŭ���̾�Ʈ ��� ����
		}
		
		void setClientInfo(){
			String[] keys = new String[clientInfo.size()];
			int[] values = new int[clientInfo.size()];
			int index = 0;
			for(Map.Entry<String, Integer> mapEntry : clientInfo.entrySet()){
			    keys[index] = mapEntry.getKey();
			    values[index] = mapEntry.getValue();
			    index++;
			}
			for(int i=0; i<clientList.size(); i++){
				sendSystemMsg(CM_ENUM.UPD_CLIST + keys[i] + " " + values[i] + "#" + i); // ��ɾ� : Ŭ���̾�Ʈ ��� ����
			}
		}
		
		void closeAll(){
			try{
				if(dos != null) dos.close();
				if(dis != null) dis.close();
				if(s != null) s.close();
			}catch(IOException ie){}
		}
		
		void filtering(String msg) { // ��ɾ� ���͸�
			String temp = msg.substring(0, 7);
			if(temp.equals(CM_ENUM.CHAT)){ // ��ɾ� : �Ϲ� ä��
				answerCheck(msg.substring(7).trim());
				sendSystemMsg(msg.substring(7));
			}else if(temp.equals(CM_ENUM.READY)){ // ��ɾ� : Ŭ���̾�Ʈ �غ� ���� üũ
				 readyPlayer++;
				 if(readyPlayer >= 2 && readyPlayer == clientList.size()){ // 2�� �̻� && ��� Ŭ���̾�Ʈ�� �غ�Ǿ��� ���
					 for(int i=3; i>0; i--){
						 try{
						 	sendSystemMsg("[ ��� �����ڵ��� �غ�Ǿ����ϴ�. ]\n[ " + i + "�� �� ������ �����մϴ� .. ]");
						 	Thread.sleep(1000);
						 }catch(InterruptedException ie){}
					 }
					 ArrayList<String> authList = new ArrayList<String>(); // ���� ������ ���� ����
					 Iterator<String> it = clientList.keySet().iterator();
					 while(it.hasNext()) authList.add(it.next());
					 Random rd = new Random();
					 sendSystemMsg(CM_ENUM.AUTH + authList.get(rd.nextInt(authList.size()))); // ��ɾ� : ���� ������ ���� ����
					 Exam ex = new Exam(); ex.start(); // ���� ����
					 StopWatch tm = new StopWatch(); tm.start(); // Ÿ�̸� ����
					 gameStart = true;
					 sendSystemMsg(CM_ENUM.START); // ��ɾ� : ���� ����
				 }
			}else if(temp.equals(CM_ENUM.MOUSE_XY)){ // ��ɾ� : ���콺 ��ǥ ����
				sendSystemMsg(msg);
			}else if(temp.equals(CM_ENUM.CHANGE_COLOR)){ // ��ɾ� : �÷� ����
				sendSystemMsg(msg);
			}else if(temp.equals(CM_ENUM.ERASE)){ // ��ɾ� : �����
				sendSystemMsg(msg);
			}else if(temp.equals(CM_ENUM.ERASE_ALL)){ // ��ɾ� : ��� �����
				sendSystemMsg(msg);
			}else if(temp.equals(CM_ENUM.GG)){ // ��ɾ� : ���� ���� (�����ڰ� ������ �������� ���)
				sendSystemMsg("[ �����ڰ� ������ �����߽��ϴ� !! ]");
				sendSystemMsg(msg);
				readyPlayer = 0;
				gameStart = false;
			}else if(temp.equals(CM_ENUM.END)){ // ��ɾ� : ���� ���� (�ð� �ʰ��� ��Ż�� �߻����� ������ ����Ǵ� ���)
				sendSystemMsg("[ ������ ����Ǿ����ϴ� !! ]");
				sendSystemMsg(msg);
				readyPlayer = 0;
				gameStart = false;
			}
		}
		
		void answerCheck(String msg){ // ���� üũ
			String tempNick = msg.substring(0, msg.indexOf(" ")); // ������ �г��� üũ
			String tempAns = msg.substring(msg.lastIndexOf(" ") + 1); // ���� ���� üũ
			if(tempAns.equals(line) && gameStart == true){ // ������ �ߺ� ������ ���� ���� ���� ���� üũ
				sendSystemMsg(CM_ENUM.END);
				gameStart = false;
				readyPlayer = 0; // ���ο� ������ �����ϱ� ���� ���� �ʱ�ȭ
				sendSystemMsg("[ " + tempNick + "�� ���� !! ]");
				clientInfo.put(tempNick, clientInfo.get(tempNick) + 1); // ������ ���� �߰�
				setClientInfo(); // ���� ǥ�ø� ���� Ŭ���̾�Ʈ ��� ����
			}
		}
	}
	
	// ���� Ŭ���� - ���� ���� ����
	class Exam extends Thread
	{
		int i = 0;
		BufferedReader br;

		public void run(){
			Random r = new Random();
			int n = r.nextInt(52);
			try{
				FileReader fr = new FileReader("wordlist.txt");
				br = new BufferedReader(fr);
				for(i=0;i<=n;i++) line = br.readLine();
				sendSystemMsg(CM_ENUM.EXAM + line);
			}catch(IOException ie){}
		}
	}
	
	// ���� Ŭ���� - Ÿ�̸�
	class StopWatch extends Thread
	{
		long preTime = System.currentTimeMillis();
		
		public void run() {
			try{
				while(gameStart == true){
					sleep(10);
					long time = System.currentTimeMillis() - preTime;
					sendSystemMsg(CM_ENUM.TIMER + (toTime(time)));
					if(toTime(time).equals("00 : 00")){
						sendSystemMsg(CM_ENUM.END); // �ð� �ʰ���, ���� ����
						readyPlayer = 0;
						gameStart = false;
						break;
					}else if(readyPlayer == 0){
						break;
					}
				}
			}catch (Exception e){}
		}
		
		String toTime(long time){
			int m = (int)(3-(time / 1000.0 / 60.0));
			int s = (int)(60-(time % (1000.0 * 60) / 1000.0));
			return String.format("%02d : %02d", m, s);
		}
	}
	
	public static void main(String[] args){
		CM_Server cms = new CM_Server();
		cms.setVisible(true);
	}
}