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
            QList.append(a)

        QList.append(random.randint(20,21))                                            ##1 coding question chosen from pool of 2   C

    elif Lang == 'P':
        for x in range(amount - 1):                                                         ##Variable amount of MC questions chosen from Q inedx 0 to 10 (C Lang questions)
            a = random.randint(10,19)
            while a in QList:
                a = random.randint(10,19)    #fixed minute error
            QList.append(a)

        QList.append(random.randint(22,23))                                            ##1 coding question chosen from pool of 2   PYTHON

    print("Debug QList:" + str(QList))
    return QList


def assessor(AInput, QType, QRef):                                             ##Qtype -> 1 = MC, 2 = SA in C  3 = SA in Python | AInput -> if QType == 1, AInput is a char; if QType == 2 / 3, AInput is the filename created by the networking class, QRef is the filename of the standard answer; 
    # gives the path of QB
    path = os.path.realpath(__file__)
    # gives the directory
    dir = os.path.dirname(path)
    dir = dir.replace("src", "questionMat")
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
        QRefT = "A" + str(QRef).strip() + ".c"
        usrOpt = compileAndExecutionC(AInput, os.path.realpath(AInput)).strip()
        opt = compileAndExecutionC(QRefT, os.path.realpath(QRefT)).strip()

        print("Debug ans: " + usrOpt + "  " + opt)

        if usrOpt == opt:
            return True
        else:
            return False

    elif QType == 3:
        ##get the standard answer Python
        print("Debug dir3:" + os.path.realpath(AInput))
        QRefT = "A" + str(QRef).strip() + ".py"
        usrOpt = compileAndExecutionPY(AInput, os.path.realpath(AInput)).strip()
        opt = compileAndExecutionPY(QRefT, os.path.realpath(QRefT)).strip()

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

                elif data == encoder("QF"):                #question list fetching request, TM sends "QF" to use this function, same logic down below

                    ack = bytes([0x03])
                    conn.sendall(ack)

                    spec = conn.recv(1024)
                    if not spec:
                        conn.sendall(encoder("error"))
                        break
                    else:
                        ack = bytes([0x04])
                        conn.sendall(ack)

                    msg = spec.decode('utf-8')
                    input = msg.split('#')

                    QList = rngQuestion(int(input[0]), str(input[1]))

                    questionBytes = (str(QList)).encode('utf-8')
                    hashCheck = hashlib.sha256(questionBytes).hexdigest().encode('utf-8')
                    conn.send(hashCheck + b' ' + questionBytes)

                    while True:
                        ack = conn.recv(1024)
                        if ack == bytes([0x04]):  #bytes ack for data
                            #print('Ack Recieved for data. Ready to send more...') #Debugging line
                            break
                        else:
                            #print("No ACK recieved yet. Retrying..., maybe due to ACK not sending or Data being corrupted") #Debug line
                            time.sleep(2)
                            conn.send(hashCheck + b' ' + questionBytes)
                            # print('Data re-sent, waiting for ACK...') #Debugging line
                    
                    conn.send(b'@')
                    print('End of data sent')

                    while True:
                        end_ack = conn.recv(1024)
                        if end_ack == bytes([0x05]):
                            print('End of data ACK recieved')
                            break
                        else:
                            print("No end of data ACK recieved yet. Retrying...")
                            time.sleep(2)
                            conn.send(b'@')
                            print('End of data sent again, waiting for ACK...')

                    conn.close()
                    #break #break out of the while loop oof data = conn.recv

                elif data == encoder("TXT"):                #question plain txt fetching request (per request)

                    ack = bytes([0x03])
                    conn.sendall(ack)

                    spec = conn.recv(1024)
                    if not spec:                            #no error checking on the recived command, too expensive
                        conn.sendall(encoder("error"))
                        break
                    else:
                        ack = bytes([0x04])
                        conn.sendall(ack)

                    msg = spec.decode('utf-8')
                    input = msg

                    # gives the path of QB
                    path = os.path.realpath(__file__)
                    # gives the directory
                    dir = os.path.dirname(path)
                    dir = dir.replace("src", "questionMat")  #Archive is not where its actaully soted
                    print("THe current dir is " + dir)
                    print("Debug dir:" + dir)
                    os.chdir(dir)

                    output = str(input) + ".txt"

                    with open(output, "r") as file:
                        answer = file.read()

                    print(answer)
                    answerBytes = (answer).encode('utf-8')
                    hashCheck = hashlib.sha256(answerBytes).hexdigest().encode('utf-8')
                    conn.send(hashCheck + b' '+ answerBytes)
                    print("Already sent and waiting")
                   
                    while True:
                        ack = conn.recv(1024)
                        if ack != bytes([0x05]):
                            time.sleep(2)
                            conn.send(hashCheck + b' ' + answerBytes)
                        else:
                            print("Reached this part")
                            break
                    
                    conn.send(b'@')
                    print('End of data sent')

                    while True:
                        end_ack = conn.recv(1024)
                        if end_ack == bytes([0x06]):
                            print('End of data ACK recieved')
                            break
                        else:
                            print("No end of data ACK recieved yet. Retrying...")
                            time.sleep(2)
                            conn.send(b'@')
                            print('End of data sent again, waiting for ACK...')

                    conn.close()

                elif data == encoder("MK"):                #question marking request (per request)

                    ack = bytes([0x03])
                    conn.sendall(ack)

                    spec = conn.recv(1024)
                    if not spec:
                        conn.sendall(encoder("error"))
                        break
                    else:
                        print('lol')

                        
                        print(spec)
                        data_c = spec.decode('utf-8')
                        print(data_c)
                        print("It was empty??")
                        hash_recieved, data_recieved = data_c.split(' ', 1)    #assuming TM will send the data in the format (hash,' ' + data)

                        hash_server = hashlib.sha256(data_recieved.encode('utf-8'))
                        hash_hex = hash_server.hexdigest()

                        if hash_hex == hash_recieved:
                            print('Data was not corrupted')
                            spec = data_recieved
                            data_ack = bytes([0x04])
                            conn.sendall(data_ack)
                        else:
                            print('Hash mismatch for recieved data, Data may be corrupted')
                        



                        #ERROR CHECKING                    *_TODO_* 
                        

                        #REMOVE HASH THAT WAS USED IN ERROR CHECKING                    *_TODO_*
                       

                        #UPDATE THE VARIABLE "spec" WITH THE CLEANED MESSAGE VALUE                    *_TODO_*

                    msg = spec
                    print(msg)
                    input = msg.split('#')

                    if(int(input[1]) != 1):
                        #need to unserealise input[0] (convert it back to student written code in plain txt) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file        *_TODO_*
                        #need to unserealise input[0] (convert it back to student written code in plain txt) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file        *_TODO_*
                        #need to unserealise input[0] (convert it back to student written code in plain txt) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file        *_TODO_*
                        #need to unserealise input[0] (convert it back to student written code in plain txt) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file        *_TODO_*

                        with open("studentA.txt", "w") as outfile:
                            outfile.write(input[0])
                        #write input[0] into this file        *_TODO_*
                        #write input[0] into this file        *_TODO_*
                        #write input[0] into this file        *_TODO_*
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
                        output = assessor(str(input[0]), int(input[1]), int(input[2]))

                    answerBytes = (str(output)).encode('utf-8')
                    hashCheck = hashlib.sha256(answerBytes).hexdigest().encode('utf-8')
                    conn.send(hashCheck + b' '+ answerBytes)
                        
                    while True:
                        ack = conn.recv(1024)
                        if ack != bytes([0x05]):
                            time.sleep(2)
                            conn.send(hashCheck + b' ' + answerBytes)
                        else:
                            break
                    
                    conn.send(b'@')
                    print('End of data sent')

                    while True:
                        end_ack = conn.recv(1024)
                        if end_ack == bytes([0x06]):
                            print('End of data ACK recieved')
                            break
                        else:
                            print("No end of data ACK recieved yet. Retrying...")
                            time.sleep(2)
                            conn.send(b'@')
                            print('End of data sent again, waiting for ACK...')

                    conn.close()

                elif data == encoder("DS"):              #sample answer fetching request (per request)

                    ack = bytes([0x03])
                    conn.sendall(ack)

                    spec = conn.recv(1024)
                    if not spec:                            #no error checking on the recived command, too expensive
                        conn.sendall(encoder("error"))
                        break
                    else:
                        ack = bytes([0x04])
                        conn.sendall(ack)

                    msg = spec.decode('utf-8')
                    input = msg

                    # gives the path of QB
                    path = os.path.realpath(__file__)
                    # gives the directory
                    dir = os.path.dirname(path)
                    dir = dir.replace("src", "questionMat")
                    print("Debug dir:" + dir)
                    os.chdir(dir)

                    output = str(input) + ".txt"

                    with open(output, "r") as file:                 #open up the file that contains the sample answer to that question and read it
                        answer = file.read()

                    answerBytes = (answer).encode('utf-8')
                    hashCheck = hashlib.sha256(answerBytes).hexdigest().encode('utf-8')
                    conn.send(hashCheck + b' '+ answerBytes)
                        
                    while True:
                        ack = conn.recv(1024)
                        if ack != bytes([0x05]):
                            time.sleep(2)
                            conn.send(hashCheck + b' ' + answerBytes)
                        else:
                            break
                            
                    conn.send(b'@')
                    print('End of data sent')

                    while True:
                        end_ack = conn.recv(1024)
                        if end_ack == bytes([0x06]):
                            print('End of data ACK recieved')
                            break
                        else:
                            print("No end of data ACK recieved yet. Retrying...")
                            time.sleep(2)
                            conn.send(b'@')
                            print('End of data sent again, waiting for ACK...')

                    conn.close()


if __name__ == "__main__":
    print("Arguments count: " + str(len(sys.argv)))
    if len(sys.argv) == 3:
        main(str(sys.argv[1]), int(sys.argv[2]))
        sys.exit(0)
    else:
        print("Illegal Arguments")
        sys.exit(1)
