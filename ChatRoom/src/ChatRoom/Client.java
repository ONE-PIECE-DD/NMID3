package ChatRoom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

//实现连接服务器的功能，同时重写run实现向服务器发送信息
public class Client implements Runnable {// 客户端
    static Socket socket = null;
    Scanner input = new Scanner(System.in);
    static String name=null;
    public static void main(String[] args) {
        System.out.println("************客户端*************");
        try {
            //输入服务器参数
            socket = new Socket("127.0.0.1", 9999);
            //连接成功后获取服务器反馈的用户名信息并打印在控制台
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Client.name=in.readLine();
            System.out.println(Client.name+"连上服务器");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //实例化Client、Read类
        Client t = new Client();
        Read r = new Read(socket);
        //开始线程实现收发数据
        Thread print = new Thread(t);
        Thread read = new Thread(r);
        print.start();
        read.start();
    }
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            //发
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            while (true) {
                //向服务器发送控制台输入的信息
                //客户端向服务器申请服务只需要在发送的语句中加入对应命令，然后由服务器实现
                String msg = input.next();
                out.println(name+":"+msg);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//重写run实现从套接字读取数据并打印在控制台
class Read implements Runnable {
    static Socket socket = null;
    public Read(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            //读取收到的信息并打印
            //会收到带有名字的客户端、服务端发来的消息，或带有>>>的服务器提示、命令语句
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                //收退出确认信息后退出程序
                if(in.readLine().equals(">>>ok")) {
                    System.out.println("closing……");
                    System.exit(0);
                }
                //正常打印收到的信息
                System.out.println( in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}