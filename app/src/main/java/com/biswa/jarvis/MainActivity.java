package com.biswa.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Maze Rush");
        setContentView(new MazeGameView(this));
    }

    static class MazeGameView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random rng = new Random();
        // Huge maze: 151 x 151 = 22,801 cells.
        int cols = 151, rows = 151;
        boolean[][] walls = new boolean[rows][cols];
        float px, py, exitX, exitY;
        ArrayList<Bot> bots = new ArrayList<>();
        ArrayList<Point> fakeExits = new ArrayList<>();
        long startAt;
        int state = 0; // 0 lobby, 1 countdown, 2 playing, 3 finished
        int countdown = 8;
        int finishRank = -1;
        long lastBotMove;
        float downX, downY;
        int mazeSeed;

        static class Bot { float x,y; boolean finished; Bot(float x,float y){this.x=x;this.y=y;} }

        MazeGameView(Context c) {
            super(c);
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            setBackgroundColor(Color.rgb(5,8,13));
        }

        void newMatch() {
            mazeSeed = rng.nextInt();
            rng.setSeed(mazeSeed);
            generateMaze();
            px=1; py=1; exitX=cols-2; exitY=rows-2;
            bots.clear();
            for(int i=0;i<9;i++) bots.add(new Bot(1,1));
            state=1;
            startAt=System.currentTimeMillis();
            finishRank=-1;
            invalidate();
        }

        // Iterative randomized DFS avoids Java stack overflow on the 151x151 maze.
        void generateMaze() {
            for(int y=0;y<rows;y++) Arrays.fill(walls[y], true);
            fakeExits.clear();
            ArrayList<Point> stack = new ArrayList<>();
            stack.add(new Point(1,1));
            walls[1][1]=false;

            int[][] ds={{2,0},{-2,0},{0,2},{0,-2}};
            while(!stack.isEmpty()) {
                Point cur=stack.get(stack.size()-1);
                ArrayList<int[]> options=new ArrayList<>();
                for(int[] d:ds){
                    int nx=cur.x+d[0], ny=cur.y+d[1];
                    if(nx>0&&nx<cols-1&&ny>0&&ny<rows-1&&walls[ny][nx]) options.add(d);
                }
                if(options.isEmpty()) { stack.remove(stack.size()-1); continue; }
                int[] d=options.get(rng.nextInt(options.size()));
                int nx=cur.x+d[0], ny=cur.y+d[1];
                walls[cur.y+d[1]/2][cur.x+d[0]/2]=false;
                walls[ny][nx]=false;
                stack.add(new Point(nx,ny));
            }

            walls[1][1]=false;
            walls[rows-2][cols-2]=false;

            // Add a few misleading openings near the edges so the maze feels less obvious.
            // They are not the real finish and send the player into dead-end sections.
            int made=0;
            while(made<8) {
                int side=rng.nextInt(4), x, y;
                if(side==0){x=1+2*rng.nextInt((cols-3)/2);y=1;}
                else if(side==1){x=1+2*rng.nextInt((cols-3)/2);y=rows-2;}
                else if(side==2){x=1;y=1+2*rng.nextInt((rows-3)/2);}
                else {x=cols-2;y=1+2*rng.nextInt((rows-3)/2);}
                if(!(x==1&&y==1)&&!(x==cols-2&&y==rows-2)&&!walls[y][x]) {
                    fakeExits.add(new Point(x,y));
                    made++;
                }
            }
        }

        boolean free(float x,float y){
            int ix=Math.round(x),iy=Math.round(y);
            return ix>=0&&iy>=0&&ix<cols&&iy<rows&&!walls[iy][ix];
        }

        void movePlayer(float dx,float dy){
            if(state!=2)return;
            float nx=px+dx,ny=py+dy;
            if(free(nx,ny)){px=nx;py=ny;}
            if(Math.round(px)==Math.round(exitX)&&Math.round(py)==Math.round(exitY)){
                finishRank=1; state=3;
            }
            invalidate();
        }

        void updateBots(){
            if(state!=2)return;
            long now=System.currentTimeMillis();
            if(now-lastBotMove<140)return;
            lastBotMove=now;
            for(Bot b:bots){
                if(b.finished)continue;
                float dx=exitX-b.x,dy=exitY-b.y;
                ArrayList<float[]> choices=new ArrayList<>();
                if(Math.abs(dx)>=Math.abs(dy)) {
                    choices.add(new float[]{Math.signum(dx),0});
                    choices.add(new float[]{0,Math.signum(dy)});
                } else {
                    choices.add(new float[]{0,Math.signum(dy)});
                    choices.add(new float[]{Math.signum(dx),0});
                }
                choices.add(new float[]{0,1});
                choices.add(new float[]{0,-1});
                choices.add(new float[]{1,0});
                choices.add(new float[]{-1,0});
                Collections.shuffle(choices,rng);
                for(float[] c:choices){
                    if(free(b.x+c[0],b.y+c[1])){b.x+=c[0];b.y+=c[1];break;}
                }
                if(Math.round(b.x)==Math.round(exitX)&&Math.round(b.y)==Math.round(exitY)) b.finished=true;
            }
            invalidate();
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            int w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(5,8,13));
            c.drawRect(0,0,w,h,p);
            p.setColor(Color.WHITE);p.setTextSize(30);c.drawText("MAZE RUSH",24,42,p);
            p.setTextSize(14);p.setColor(Color.LTGRAY);c.drawText("10 PLAYERS  •  151×151  •  FIND THE REAL EXIT",24,66,p);
            if(state==0){drawLobby(c,w,h);return;}

            int top=82;
            float cell=Math.min((w-24f)/15f,(h-top-90f)/15f);
            // Camera follows the player. Only a 15x15 area around the player is drawn.
            int viewRadius=7;
            int pcx=Math.round(px), pcy=Math.round(py);
            int minX=Math.max(0,pcx-viewRadius), maxX=Math.min(cols-1,pcx+viewRadius);
            int minY=Math.max(0,pcy-viewRadius), maxY=Math.min(rows-1,pcy+viewRadius);
            float ox=w/2f-(pcx+0.5f)*cell;
            float oy=top+(h-top-90f)/2f-(pcy+0.5f)*cell;

            for(int y=minY;y<=maxY;y++) for(int x=minX;x<=maxX;x++) {
                p.setColor(walls[y][x]?Color.rgb(24,30,41):Color.rgb(10,15,23));
                c.drawRect(ox+x*cell,oy+y*cell,ox+(x+1)*cell-1,oy+(y+1)*cell-1,p);
            }

            // Fake exits: tempting doors that do NOT finish the match.
            p.setColor(Color.rgb(220,150,55));
            for(Point q:fakeExits) {
                if(Math.abs(q.x-pcx)<=viewRadius&&Math.abs(q.y-pcy)<=viewRadius)
                    c.drawRect(ox+q.x*cell,oy+q.y*cell,ox+(q.x+1)*cell,oy+(q.y+1)*cell,p);
            }

            if(Math.abs(exitX-pcx)<=viewRadius&&Math.abs(exitY-pcy)<=viewRadius) {
                p.setColor(Color.rgb(45,235,115));
                c.drawRect(ox+exitX*cell,oy+exitY*cell,ox+(exitX+1)*cell,oy+(exitY+1)*cell,p);
            }

            p.setColor(Color.rgb(245,80,80));
            for(Bot b:bots) {
                if(b.finished)continue;
                if(Math.abs(b.x-pcx)<=viewRadius&&Math.abs(b.y-pcy)<=viewRadius)
                    c.drawCircle(ox+(b.x+.5f)*cell,oy+(b.y+.5f)*cell,cell*.25f,p);
            }

            p.setColor(Color.rgb(75,175,255));
            c.drawCircle(ox+(px+.5f)*cell,oy+(py+.5f)*cell,cell*.32f,p);

            if(state==1){
                int left=Math.max(0,countdown-(int)((System.currentTimeMillis()-startAt)/1000));
                p.setTextSize(42);p.setColor(Color.WHITE);c.drawText("START "+left,w/2-75,top+45,p);
                if(left==0){state=2;lastBotMove=0;}
                postInvalidateDelayed(100);
            }
            if(state==2){
                p.setTextSize(15);p.setColor(Color.WHITE);
                c.drawText("SWIPE TO MOVE  •  Green = REAL EXIT  •  Gold = FAKE EXIT",18,h-34,p);
                updateBots();
            }
            if(state==3){
                p.setColor(Color.argb(235,5,8,12));c.drawRect(0,0,w,h,p);
                p.setColor(Color.WHITE);p.setTextSize(42);c.drawText("FINISHED!",w/2-115,h/2-30,p);
                p.setTextSize(24);c.drawText("RANK #"+finishRank,w/2-65,h/2+15,p);
                p.setTextSize(16);c.drawText("Tap to play again",w/2-70,h/2+55,p);
            }
        }

        void drawLobby(Canvas c,int w,int h){
            p.setColor(Color.WHITE);p.setTextSize(28);c.drawText("10 PLAYER ESCAPE",24,130,p);
            p.setColor(Color.LTGRAY);p.setTextSize(17);c.drawText("Waiting room",24,165,p);
            p.setColor(Color.rgb(70,190,255));c.drawRoundRect(24,190,w-24,255,18,18,p);
            p.setColor(Color.BLACK);p.setTextSize(20);c.drawText("PLAY",w/2-28,231,p);
            p.setColor(Color.GRAY);p.setTextSize(14);
            c.drawText("151×151 huge maze • new maze every match",24,295,p);
            c.drawText("Fake exits + deep dead ends • bots fill empty slots",24,320,p);
            c.drawText("No database • prototype mode",24,345,p);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                downX=e.getX();downY=e.getY();
                if(state==0){newMatch();return true;}
                if(state==3){newMatch();return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP&&state==2){
                float dx=e.getX()-downX,dy=e.getY()-downY;
                if(Math.max(Math.abs(dx),Math.abs(dy))<20)return true;
                if(Math.abs(dx)>Math.abs(dy))movePlayer(Math.signum(dx),0);
                else movePlayer(0,Math.signum(dy));
                return true;
            }
            return true;
        }
    }
}
