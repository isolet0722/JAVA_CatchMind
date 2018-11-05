import java.awt.event.*;
import javax.swing.*;

public class CM_Login extends CM_Login_GUI implements ActionListener
{
	public static String ip, nickName;

	public CM_Login(){
		btn_Connect.addActionListener(this);
		btn_Exit.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == btn_Connect){
			if(tf_nickName.getText().equals("")){
				JOptionPane.showMessageDialog(null, "�г����� �Է��� �ּ���!", "ERROR!", JOptionPane.WARNING_MESSAGE);
			}else if(tf_Ip.getText().equals("")){
				JOptionPane.showMessageDialog(null, "IP �ּҸ� �Է��� �ּ���!", "ERROR!", JOptionPane.WARNING_MESSAGE);
			}else if(tf_nickName.getText().trim().length() > 5){
				JOptionPane.showMessageDialog(null, "�г����� 5���ڱ����� �Է��� �� �ֽ��ϴ�!", "ERROR!", JOptionPane.WARNING_MESSAGE);
				tf_nickName.setText("");
			}else{
				nickName = tf_nickName.getText().trim();
				String temp = tf_Ip.getText();
				if(temp.matches("(^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$)")){
					ip = temp;
					JOptionPane.showMessageDialog(null, "             �α��� ����!", "JAVA CatchMind LOGIN", JOptionPane.INFORMATION_MESSAGE);
					btn_Connect.setEnabled(false);
					tf_nickName.setEnabled(false);
					tf_Ip.setEnabled(false);
					setVisible(false);
					
					new CM_Client();
				}else{
					JOptionPane.showMessageDialog(null, "IP �ּҸ� ��Ȯ�ϰ� �Է��� �ּ���! ", "ERROR!", JOptionPane.WARNING_MESSAGE);
				}
			}
		}else if(e.getSource() == btn_Exit){
			System.exit(0);
		}
	}

	public static void main(String[] args){
		new CM_Login();
	}
}