package ChatRoom;
import com.sun.deploy.security.SelectableSecurityManager;
import com.sun.deploy.security.UserDeclinedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Server implements Runnable {//服务端操作套接字，同时通过重写run实现读取数据的操作
    //初始化基本参数：储存套接字的链表、用户套接字、服务器套接字
    static List<Socket> socketList=new ArrayList<Socket>();         //储存连入的所有套接字
    static Socket socket = null;            //用于获取新的套接字
    static ServerSocket serverSocket = null;            //设置本地服务器的套接字供客户连接
    //设置服务器端口号
    public Server() {
        try {
            serverSocket = new ServerSocket(9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //主函数
    public static void main(String[] args) {

        System.out.println("************Server*************");
        //实例化服务器类
        Server t = new Server();
        String nameofclient = null;
        int count = 0;      //记录当前连入的用户数量
        while (true) {
            try {
                System.out.println("端口9999等待被连接......");
                //获取新套接字
                socket = serverSocket.accept();
                //用户数量加1
                count++;
                System.out.println("第" + count + "个客户已连接");
                //当新用户加入房间时反馈服务器生成的用户名
                nameofclient = "User"+count;
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(nameofclient);
                out.flush();
                //储存相应的套接字
                socketList.add(socket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //将新加入的客户的套接字储存在服务器发送数据的类中便于服务器广播信息
            Print p = new Print(socket);
            //实例化将数据在控制台显示并将数据广播出去的类
            Thread read = new Thread(t);
            //实例化实现控制台输入的数据并广播出去的类
            Thread print = new Thread(p);
            //开启两个线程
            read.start();
            print.start();
        }
    }


    @Override
    public void run() {
        //退出请求标志
        boolean exit=false;
        //私聊消息标记
        boolean p2p=false;
        String filter=null;
        String InfOfp2p=null;
        String OrderOfClient_str = null;
        int OrderOfClient = 0;
        // 重写run方法，实现转发、处理客户端发来的信息或请求语句
        try {
            Thread.sleep(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));//获取传送过来的数据
            while (true) {
                //控制台显示客户发来的信息
                String Lineofrecept = in.readLine();
                System.out.println( Lineofrecept);
                //处理客户端发来的退出聊天室请求
                if(Lineofrecept.contains(">>>exit"))
                {
                    //将退出请求修改，之后广播出去
                    Lineofrecept="(系统提示)"+Lineofrecept;
                    //标志该消息为退出请求
                    exit=true;
                }
                //处理客户端发来的私聊请求
                if(Lineofrecept.contains(">>>for"))
                {
                    filter=Lineofrecept;
                    //获取私聊对象的序号
                    OrderOfClient_str = filter.replaceAll(".*[^\\d](?=(\\d+))","");
                    //转为常数
                    OrderOfClient=Integer.parseInt(OrderOfClient_str);
                    //获取私聊的消息
                    InfOfp2p = filter.replaceAll(OrderOfClient_str,"");
                    //标志该消息为私聊消息
                    p2p=true;
                }
                //正常向其它客户端广播客户发送的正常信息和其它特殊信息
                for (int i = 0; i < socketList.size(); i++) {
                    if(p2p){//私聊请求反馈信息，提示客户输入私聊对象
                        //向指定对象发送消息后退出循环，并将私聊标记还原
                        PrintWriter out = new PrintWriter(socketList.get(OrderOfClient-1).getOutputStream());
                        out.println(InfOfp2p);
                        out.flush();
                        p2p=false;
                        break;
                    }
                    //正常消息广播
                    Socket socket=socketList.get(i);
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    if (socket!=this.socket) {
                        out.println(Lineofrecept);
                        out.flush();
                    }else{
                        //当
                        if(exit) {
                            //反馈退出请求，由服务器端控制客户端退出程序，并将退出标识符还原
                            out.println(">>>ok");
                            out.flush();
                            //从表中删除该客户端的信息
                            socketList.remove(this.socket);
                            i--;
                            exit=false;
                        }
                        continue;
                    }
                    out.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Print implements Runnable {//重写run实现
    //记录所有连入的用户信息
    static List<Socket> socketList=new ArrayList<Socket>();
    Scanner input = new Scanner(System.in);
    public Print(Socket s) {// 构造方法
        try {
            socketList.add(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() { //服务端主动向客户端发送消息，以及处理服务器控制台输入的命令语句
        try {
            Thread.sleep(1000);
            while (true) {
                //从控制台获取消息
                String msg = input.nextLine();
                //处理服务器控制台输入的命令：查询当前聊天室用户并打印所有用户的信息
                if(msg.equals(">>>list of clients")) {
                    for (int i = 0; i < socketList.size(); i++) {
                        System.out.println("User"+i+":"+socketList);
                    }
                }
                else {//正常向客户广播服务器控制端输入的消息
                    for (int i = 0; i < socketList.size(); i++) {
                        Socket socket = socketList.get(i);
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        // System.out.println("对客户端说：");
                        out.println("(Server)>>>：" + msg);
                        out.flush();
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}