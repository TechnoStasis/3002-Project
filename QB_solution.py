import subprocess
import random
import os
import socket
import sys


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
    with socket.socket(socket.AF_INET,socket.SOCK_STREAM) as server_socket:
        server_socket.bind((HOST, PORT))   #Bind takes in a tuple
        server_socket.listen()
        hostname = socket.gethostname()
        ip_address = socket.gethostbyname(hostname)
        print(f'Server is lisetning on {HOST} : {PORT}')
        print(f'The IP address of {hostname} os {ip_address}')

        while True:
            conn, addr = server_socket.accept()
            print(f'New Client connected" {addr}')

            syn = bytes([0x01])  #send synchronisation message
            conn.send(syn)
            print('SYN sent')

            syn_ack = conn.recv(1024)
            if syn_ack == bytes([0x02]):  #Acknowledge recieved
                print('SYN-ACK recieved')

                ack = bytes([0x03])  #Server send acknowled
                conn.send(ack)
                print('ACK sent')


                data = conn.recv(1024)
                message = data.decode('utf-8')
                print("Recieved message from the Client:", message)

                if message == encoder("QF"):                #question fetching request, TM sends "QF" to use this function, same logic down below
                    ack = bytes([0x03])                     #Server send ACK, make this a special byte so that the TM can recogonize it and send the amount and question type straight away (format is "amount#type", e.g. "10#C")
                    conn.send(ack)
                    print('ACK sent')

                    spec = conn.recv(1024)
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

                    for element in range(len(Qlist)):
                        fName = str(element) + ".txt"
                        output.append(fName)

                    with open("result.txt", "wb") as outfile:
                        for f in output:
                            with open(f, "rb") as infile:
                                outfile.write(infile.read())

                            outfile.write("#\n")                          #each distinct question is # sperated

                    payload =                                                 #send payload "result.txt" (a text file of all question generated), NEED to serialize it (to JSON) before sending it        *_TODO_*
                    message_bytes = payload.encode('utf-8')
                    conn.send(message_bytes)

                elif message == encoder("MK"):                #question marking request
                    ack = bytes([0x03])                       #Server send ACK, make this a special byte so that the TM can recogonize it and send the answer, question type and question ID straight away (format is e.g. "D#1#16")
                    conn.send(ack)
                    print('ACK sent')

                    spec = conn.recv(1024)
                    msg = spec.decode('utf-8')
                    input = msg.split('#')

                    if(int(input[1]) != 1):
                        #need to unserealise input[0] (aka. student code) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file        *_TODO_*

                    else:
                        output = assessor(str(input[0]), int(input[1]), int(input[2]))          ##need to unserealise input[0] (aka. student code) when QType = 2 or 3, in to a plain txt file then rename it into a C or python file

                    payload =                                                 #send payload "output", NEED to serialize it (to JSON) before sending it        *_TODO_*
                    message_bytes = payload.encode('utf-8')
                    conn.send(message_bytes)

                elif message == encoder("DS"):              #sample answer fetching request
                    ack = bytes([0x03])                     #Server send ACK, make this a special byte so that the TM can recogonize it and send the question IDs straight away (format is e.g. "10#12#5#2#7#22#19#1")
                    conn.send(ack)
                    print('ACK sent')

                    spec = conn.recv(1024)
                    msg = spec.decode('utf-8')
                    input = msg.split('#')

                    #TODO#


                conn.close()
            else:
                print("Failed to recieve acknlowedgements or send")


if __name__ == "__main__":
    print("Arguments count: " + len(sys.argv))
    if len(sys.argv) == 3:
        main(sys.argv[1], sys.argv[2])
        sys.exit(0)

    else:
        print("Illegal arguments")
        sys.exit(1)