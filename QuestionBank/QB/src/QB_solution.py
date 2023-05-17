import subprocess
import random
import os


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


def rngQuestion():
    QList = []
    for x in range(8):                                                         ##8 MC questions chosen from pool of 14
        a = random.randint(0,14)
        while a in QList:
            a = random.randint(0,14)
            
        Qlist.add(a)

    Qlist.add(random.randint(15,16))                                            ##1 coding question chosen from pool of 2   PYTHON
    Qlist.add(random.randint(17,18))                                            ##1 coding question chosen from pool of 2   C

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
        
        if AInput == ans[QRef - 1]:
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


print(assessor("Q15.c", 2, "Q15A.c"))