import subprocess
import random
import os
import socket
import sys
import time
import hashlib


def compileAndExecutionC(fileName, filePath):                                     ##usage: fileName -> helloWorld.c      filePath -> home/user/.../helloWorld.c

    objName = (fileName.strip())[:-2]
    path = filePath.strip()
    exeArg = "./" + objName
    ans = ""

    processCompile = subprocess.Popen(['cc', '-o', objName, path], 
                                stdout=subprocess.PIPE, 
                                stderr=subprocess.PIPE,
                                universal_newlines=True)


    while True:
            outputCompile = processCompile.stdout.readline()                    ##debug use only lines
            print(outputCompile.strip())                                        ##
            # Do something else
            returnCodeCompile = processCompile.poll()
            if returnCodeCompile is not None:

                process = subprocess.Popen([exeArg], 
                                    stdout=subprocess.PIPE, 
                                    stderr=subprocess.PIPE,
                                    universal_newlines=True)

                while True:
                    return_code = process.poll()
                    if return_code is not None:
                        print('RETURN CODE', return_code)
                        # Process has finished, read rest of the output 
                        for output in process.stdout.readlines():
                            print("Debug opt:" + output.strip())
                            ans = output.strip() + ans
                        break
                break

    return ans


def compileAndExecutionPY(fileName, filePath):

    path = filePath.strip()
    exeArg = path
    ans = ""

    processCompile = subprocess.Popen(['python3', path], 
                                stdout=subprocess.PIPE, 
                                stderr=subprocess.PIPE,
                                universal_newlines=True)


    while True:
            outputCompile = processCompile.stdout.readline()                    ##debug use only lines
            print(outputCompile.strip())                                        ##
            # Do something else
            returnCodeCompile = processCompile.poll()
            if returnCodeCompile is not None:

                process = subprocess.Popen(['python3', exeArg], 
                                    stdout=subprocess.PIPE, 
                                    stderr=subprocess.PIPE,
                                    universal_newlines=True)

                while True:
                    return_code = process.poll()
                    if return_code is not None:
                        print('RETURN CODE', return_code)
                        # Process has finished, read rest of the output 
                        for output in process.stdout.readlines():
                            print("Debug opt:" + output.strip())
                            ans = output.strip() + ans
                        break
                break

    return ans


def rngQuestion(amount, Lang):
    QList = []

    if Lang == 'C':
        for x in range(amount - 1):                                                         ##Variable amount of MC questions chosen from Q inedx 0 to 10 (C Lang questions)
            a = random.randint(0,9)
            while a in QList:
                a = random.randint(0,9)         
            Qlist.add(a)

        Qlist.add(random.randint(20,21))                                            ##1 coding question chosen from pool of 2   C

    elif Lang == 'P':
        for x in range(amount - 1):                                                         ##Variable amount of MC questions chosen from Q inedx 0 to 10 (C Lang questions)
            a = random.randint(10,19)
            while a in QList:
                a = random.randint(10,19)
            Qlist.add(a)

        Qlist.add(random.randint(22,23))                                            ##1 coding question chosen from pool of 2   PYTHON

    print("Debug QList:" + Qlist)
    return QList


def assessor(AInput, QType, QRef):                                             ##Qtype -> 1 = MC, 2 = SA in C  3 = SA in Python | AInput -> if QType == 1, AInput is a char; if QType == 2 / 3, AInput is the filename created by the networking class, QRef is the filename of the standard answer; 
    # gives the path of QB
    path = os.path.realpath(__file__)
    # gives the directory
    dir = os.path.dirname(path)
    dir = dir.replace("src", "questionArchive")
    print("Debug dir:" + dir)
    os.chdir(dir)

    if QType == 1:
        fileA = open("MCAnswers.txt", 'r')
        ans = fileA.readline()
        
        if AInput == ans[QRef]:
            return True
        else:
            return False

    elif QType == 2:
        ##get the standard answer C
        print("Debug dir2:" + os.path.realpath(AInput))
        usrOpt = compileAndExecutionC(AInput, os.path.realpath(AInput)).strip()
        opt = compileAndExecutionC(QRef, os.path.realpath(QRef)).strip()

        print("Debug ans: " + usrOpt + "  " + opt)

        if usrOpt == opt:
            return True
        else:
            return False

    elif QType == 3:
        ##get the standard answer Python
        print("Debug dir3:" + os.path.realpath(AInput))
        usrOpt = compileAndExecutionPY(AInput, os.path.realpath(AInput)).strip()
        opt = compileAndExecutionPY(QRef, os.path.realpath(QRef)).strip()

        if usrOpt == opt:
            return True
        else:
            return False


def encoder(str):
    return str.encode('utf-8')


def main(HOST, PORT):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen(5)
        print(f"Server is listening on {HOST}:{PORT}")
        conn, addr = s.accept()
        with conn:
            print(f"Connected by {addr}")
            while True:
                data = conn.recv(1024)
                if not data:
                    break

                elif data == encoder("QF"):                #question fetching request, TM sends "QF" to use this function, same logic down below

                    conn.sendall(data)

                    spec = conn.recv(1024)
                    if not spec:
                        conn.sendall(encoder("error"))
                        break

                    msg = spec.decode('utf-8')
                    input = msg.split('#')

                    QList = rngQuestion(int(input[0]), str(input[1]))

                    # gives the path of QB
                    path = os.path.realpath(__file__)
                    # gives the directory
                    dir = os.path.dirname(path)
                    dir = dir.replace("src", "questionArchive")
                    print("Debug dir:" + dir)
                    os.chdir(dir)
                    
                    output = []

                    for element in Qlist:
                        fName = str(element) + ".txt"
                        output.append(fName)

                    with open("result.txt", "wb") as outfile:
                        for f in output:
                            with open(f, "rb") as infile:
                                outfile.write(infile.read())

                            outfile.write("#\n")                          #each distinct question is # sperated

                        outfile.write(QList)                              #writes the Question refrence ID list to the last line of the file for TM to read             *_MAY NEED FIXING_*

                    with open("result.txt",'r') as file:     #re-open question file to send over data to TM
                        questions = file.read().split("#")

                    for question in questions:
                        question_bytes = (question + '#').encode('utf-8') #add back hashtag since TM needs it
                        hash_check = hashlib.sha256(question_bytes).hexdigest().encode('utf-8') # Needed for data corruption check
                        conn.send(hash_check + b' ' + question_bytes)
                       # print('Question and hash sent, waiting for ACK..') #Debugging line

                        while True:
                            ack = conn.recv(1024)
                            if ack == bytes([0x04]):  #bytes ack for data
                                #print('Ack Recieved for data. Ready to send more...') #Debugging line
                                break
                            else:
                                #print("No ACK recieved yet. Retrying..., maybe due to ACK not sending or Data being corrupted") #Debug line
                                time.sleep(2)
                                conn.send(hash_check + b' ' + question_bytes)
                               # print('Data re-sent, waiting for ACK...') #Debugging line
                    
                    conn.send(b'@')  #character '@' used to communicate end of data
                    #print('End of data sent')  #Debugging line

                    while True:
                        end_ack = conn.recv(1024)
                        if end_ack == bytes([0x05]):
                            #print('End of data ACK rcieved') #Debugging line
                            break
                        else:
                            print("No end of data ACK recieved yet. Retrying...")
                            time.sleep(2)
                            conn.send(b'@')
                           # print('End of data sent again, waiting for ACK...') #Debugging line

                    conn.close()

                elif data == encoder("MK"):                #question marking request (per request)

                    conn.sendall(data)

                    spec = conn.recv(1024)
                    if not spec:
                        conn.sendall(encoder("error"))
                        break

                    msg = spec.decode('utf-8')
                    input = msg.split('#')

                    if(int(input[1]) != 1):
                        #need to unserealise input[0] (aka. student written code) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file        *_TODO_*

                        with open("studentA.txt", "wb") as outfile:
                        #write input[0] into this file        *_TODO_*

                        oldPath = os.path.realpath("studentA.txt")

                        if input[1] == 2:
                            newPath = str(os.path.realpath("studentA.txt"))[:-4] + ".c"
                            os.rename(oldPath, newPath)
                        elif input[1] == 3:
                            newPath = str(os.path.realpath("studentA.txt"))[:-4] + ".py"
                            os.rename(oldPath, newPath)

                        output = assessor(newPath, int(input[1]), int(input[2]))

                    else:
                        output = assessor(str(input[0]), int(input[1]), int(input[2]))          ##need to unserealise input[0] (aka. student code) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file

                    payload =                                                 #send payload "output", NEED to serialize it (to JSON) before sending it        *_TODO_*
                    messageBytes = payload.encode('utf-8')
                    conn.sendall(messageBytes)

                elif data == encoder("DS"):              #sample answer fetching request (per request)

                    conn.sendall(data)

                    spec = conn.recv(1024)
                    if not spec:
                        conn.sendall(encoder("error"))
                        break

                    msg = spec.decode('utf-8')
                    input = msg

                    # gives the path of QB
                    path = os.path.realpath(__file__)
                    # gives the directory
                    dir = os.path.dirname(path)
                    dir = dir.replace("src", "questionMat")
                    print("Debug dir:" + dir)
                    os.chdir(dir)

                    output = ""

                    fName = str(input) + ".txt"
                    output = fName
                        
                    with open("result.txt", "wb") as outfile:
                        with open(output, "rb") as infile:
                            outfile.write(infile.read())

                            outfile.write("#\n")                          #each distinct sample answer is # sperated

                    with open("result.txt","r") as file:
                        answers = file.read().split("#")           #same process as for sending questions
                    
                    for answer in answers:
                        answer_bytes = (answer +'#').encode('utf-8')
                        hash_check = hashlib.sha256(answer_bytes).hexdigest().encode('utf-8')
                        conn.send(hash_check + b' '+ answer_bytes)
                        
                        while True:
                            ack = conn.recv(1024)
                            if ack != bytes([0x04]):
                                time.sleep(2)
                                conn.send(hash_check + b' ' + answer_bytes)
                    
                    conn.send(b'@')

                    while True:
                        end_ack = conn.recv(1024)
                        if end_ack == bytes([0x05]):
                            conn.close()
                            break
                        else:
                            time.sleep(2)
                            conn.send(b'@')


if __name__ == "__main__":
    print("Arguments count: " + len(sys.argv))
    if len(sys.argv) == 3:
        main(sys.argv[1], sys.argv[2])
        sys.exit(0)

    else:
        print("Illegal arguments")
        sys.exit(1)