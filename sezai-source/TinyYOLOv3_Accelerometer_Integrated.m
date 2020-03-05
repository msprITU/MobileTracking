% initialize Matlab
clc;
clear all;
close all;
for vid = 1:19
    
% read accelerometer data
[frame, accX, accY, accZ, dT] = textread(['C:\Users\Sezai Burak\Desktop\Dönem\Bitirme_Projesi\sezai\ivmeverileri\',num2str(vid),'.txt'], '%d %f %f %f %d', 'delimiter', ',');

% read detection data
[a, b, class, posX, posY, width, height, conf, a1,a2, a3] = ...
    textread(['C:\Users\Sezai Burak\Desktop\Dönem\Bitirme_Projesi\sezai\yoloverileri\',num2str(vid),'_Result.txt'], '%d %d %s %d %d %d %d %f %f %f %f', 'delimiter', ',');
finalPos=zeros(frame(end),5);

% calculate velocity
camVelX = cumsum(dT .* accX);
camVelY = cumsum(dT .* accY);

% read raw video
vidReader = VideoReader(['C:\Users\Sezai Burak\Desktop\Dönem\Bitirme_Projesi\sezai\allvideos\',num2str(vid),'.avi']);

% initialize output video
outputVideo = VideoWriter(['C:\Users\Sezai Burak\Desktop\Dönem\Bitirme_Projesi\sezai\yez integrated sezai v2\',num2str(vid),'_Integrated.avi']);
outputVideo.FrameRate = 3;
open(outputVideo);

% initialize variables
alpha = 0.04;


%%ilk5 car , 6-11 person, 12 car

targetClass2 = ["car", "car", "truck", "car", "car", "person", "person", "person", "person", "person", "person", "car", "car", "car", "car", "car", "dog", "cat", "dog"];
targetClass = targetClass2(vid);


% if vid == 3
% targetClass = 'truck';  
% elseif vid < 6 || vid > 11
% targetClass = 'car';
% else
% targetClass = 'person';  
% end

ic = 1;
tempX = 0;
tempY = 0;

% lastPos = [posX(1), posY(1), width(1), height(1)];
% lastPos'u YOLO dan al?nan veri ile tanimliyoruz.

son=find(posX(6:end));
kac=son(1)+5;
lastPos=[posX(son(1)+5), posY(son(1)+5), width(son(1)+5), height(son(1)+5)];
deltaX = 0;
deltaY = 0;
%%%% ILK KONUM OLARAK YOLO ILE OKUNAN ILK FRAME ALINIYOR- 1.FRAME


% process frame by frame
while hasFrame(vidReader)

    % skip first 5 frames
    if ic < son(1)+6
        frameRGB = readFrame(vidReader);
        ic = ic + 1;
        continue;
    end
    %%%%6.FRAMEDEN BASLANMASI ISTENILDIGI ICIN ILK 5 FRAME ATLANIYOR
% % if ic==14
% % ic;
% % end
    % temporary velocities
    tempX = camVelX(ic - 5);
    tempY = camVelY(ic - 5);
    %%%%6.FRAMEDE KARSILIK GELEN IVME VERISI 1.SATIR OLDUGU ICIN -5 YAPILIYOR
    
    rowNew = find(a==ic);
    rowNew2 = find(a==ic-1);
    

    % if the target object found by YOLO update delta positions
    if ic ~= 1 && strcmp(class(rowNew(1)), targetClass) && strcmp(class(rowNew2(1)), targetClass)
        deltaX = posX(rowNew(1)) - posX(rowNew2(1)) - tempY * alpha;
        deltaY = posY(rowNew(1)) - posY(rowNew2(1)) - tempX * alpha;
    end
    
    % failPos is the alternative position which is used when YOLO FAILS
    failPos = [lastPos(1) + tempY * alpha + deltaX, lastPos(2) + tempX * alpha + deltaY, lastPos(3), lastPos(4)];
   
    %%%ILK SSD yoksa
    
    % rPos is the alternative position from acc which is used when YOLO WORKS
    rPos = [lastPos(1) + camVelY(ic - 5) * alpha + deltaX, lastPos(2) + camVelX(ic - 5) * alpha + deltaY, lastPos(3), lastPos(4)];

    % ABOUT failPos AND rPos EQUATIONS:
    % Both equations are using lastPos which is the last known position of
    % the target object. At each frame, first, equations are using lastPos,
    % then the lastPos is updated. Since it is updated after using it is
    % always the last known position, NOT the current position.
    % Delta positions (deltaX and deltaY) are updating only when the target
    % object is not found by YOLO. So, while executing equations,
    % the terms tempX*alpha in equation and in deltaX is NOT cancelling
    % each other, they are belongs to different frames actually (Remember,
    % failPos is using only when YOLO FAILS).    
    
    % set last position KONTROL ET FILIZ
% %     if strcmp(class(ic), targetClass) % update lastPos when YOLO WORKS
% %         lastPos = [posX(ic), posY(ic), width(ic), height(ic)];
% %         failPos = lastPos;
% %     elseif strcmp(class(ic), 'NOTHING') % update lastPos when YOLO FAILS
% %         lastPos = failPos;
% %     end
    
    % read frame
    frameRGB = readFrame(vidReader);
    
    % show frame
    fig = imshow(frameRGB); 
    hold on

    % when the target object found by YOLO
    if strcmp(class(rowNew(1)), targetClass)
        
        % draw cyan rectangle, previously was green.
        gPos = [posX(rowNew(1)), posY(rowNew(1)), width(rowNew(1)), height(rowNew(1))];
        rectangle('Position', gPos, 'Edgecolor', 'c', 'LineWidth', 4.5);
        
        % draw red rectangle
        rectangle('Position', rPos, 'Edgecolor', 'r', 'LineWidth', 4.5)
        
        % final decision calculations
        % check IoU
        if bboxOverlapRatio(gPos, rPos) > 0.7
            rectangle('Position', gPos, 'Edgecolor', 'b', 'LineWidth', 4.5, 'LineStyle', '--');
            lastPos=gPos;
            finalPos(ic,:)=[gPos 1];
        else
            % check confidence score
            if conf(rowNew(1)) > 0.6
                rectangle('Position', gPos, 'Edgecolor', 'b', 'LineWidth', 4.5, 'LineStyle', '--');
            lastPos=gPos;
            finalPos(ic,:)=[gPos 1];
            gPos
            ic
            else
                % check image plane boundaries
                if rPos(1) < 1280 && rPos(1) > 0 && rPos(2) < 720 && rPos(2) > 0
                    rectangle('Position', rPos, 'Edgecolor', 'b', 'LineWidth', 4.5, 'LineStyle', '--');
               lastPos=rPos;
                 finalPos(ic,:)=[rPos 0];
                else
                    rectangle('Position', gPos, 'Edgecolor', 'b', 'LineWidth', 4.5, 'LineStyle', '--');
                 lastPos=gPos;
                   finalPos(ic,:)=[gPos -3];
                end
            end
        end
    
    
    % when the target object NOT found by YOLO
    else 
       
        rectangle('Position', failPos, 'Edgecolor', 'r', 'LineWidth', 4.5);
        
        lastPos=failPos;
        
          if failPos(1) < 1280 && failPos(1) > 0 && failPos(2) < 720 && failPos(2) > 0
              
              finalPos(ic,:)=[failPos -1];
              
          else
              
              finalPos(ic,:)=[failPos -2];
              
          end
          
        rectangle('Position', failPos, 'Edgecolor', 'b', 'LineWidth', 4.5, 'LineStyle', '--');
         finalPos(ic,:)=[failPos -2];
    end
    

    hold off
    
%     saveas(fig, strcat('C:\Users\ULAS\Desktop\OpticFlow\Frames\', num2str(ic), '.jpg'));      
    
    % write output video
    writeVideo(outputVideo, getframe);
    
    ic = ic + 1;
    
end
dlmwrite(['finalBBwithIntegratedSezaiv2_',num2str(vid),'_5frameKaymali.txt'],finalPos)
close(outputVideo);
close all
clearvars -except vid
end

