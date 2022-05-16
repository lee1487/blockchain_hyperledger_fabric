# 하이퍼레저 설정
```

Ubuntu 18.04 설치
네트워크 구성 
https://www.youtube.com/watch?v=CKUv3oEI8Yo&t=363s  32분 참조


---------------------------------------------------------
하이퍼레저 설치전 기본 설치 구성
---------------------------------------------------------
1. curl 설치
$ sudo apt install curl 
$ curl -V   7.58.0

2. 도커 설치
$ curl -fsSL https://get.docker.com/ | sudo sh
$ sudo usermod -aG docker $USER
$ sudo reboot
$ docker -v  20.10.14

3. docker-compose 설치
$ sudo curl -L "https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
$ sudo chmod +x /usr/local/bin/docker-compose
$ docker-compose --version 1.22.0

4. go 설치
$ cd /usr/local
$ sudo wget https://storage.googleapis.com/golang/go1.11.1.linux-amd64.tar.gz
$ sudo tar -C /usr/local -xzf go1.11.1.linux-amd64.tar.gz
$ echo 'export PATH=$PATH:/usr/local/go/bin' | sudo tee -a /etc/profile && \
echo 'export GOPATH=$HOME/go' | tee -a $HOME/.bashrc && \
echo 'export PATH=$PATH:$GOROOT/bin:$GOPATH/bin' | tee -a $HOME/.bashrc && \
mkdir -p $HOME/go/{src,pkg,bin}
$ sudo reboot
$ go version	1.11.1

5. git 설치
$ sudo apt install -y git 
$ git --version		2.17.1

6. python설치
$ sudo apt install -y python
$ python --version  2.7.17

7. node, npm 설치
$ curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.2/install.sh | bash
$ sudo reboot
$ nvm install 8			node version 8.17.0
$ nvm use 8
$ npm install npm@5.6.0 -g

8. java 설치
$ sudo add-apt-repository ppa:openjdk-r/ppa
$ sudo apt update
$ sudo apt install openjdk-8-jdk openjdk-8-jre
$ javac -version
$ java -version		1.8.0_312

9. gradle 설치
$ sudo apt install gradle
$ gradle -v			4.4.1

10. 하이퍼레저 패브릭 설치 
$ cd $GOPATH/src
$ curl -sSL https://bit.ly/2ysbOFE | bash -s -- 1.4.12 1.4.9 0.4.22(안정화 버전)
  -> 1.4.3 1.4.3 0.4.15로 설치후 테스트 진행시 에러 발생 안정화 버전 찾는데 오래걸림


---------------------------------------------------------
하이퍼레저 패브릭 시작하기
  generate: 네트워크 구성 및 환경 설정 파일을 생성한다.
  up: BYFN 네트워크에 관련된 도커 컨테이너를 생성하고 실행한다.
  down: 도커 컨테이너로 실행됐던 네트워크를 종료하고 관련 파일을 삭제헤 설정을 초기화한다 
  
쉘 스크립트의 동작은 모든 커맨드 명령어의 진행을 자동으로 수행하게 돼 있다. 그러나 자동으로 
실행될 경우 어떤 일이 일어나는지 이해하기 어렵기 때문에 실제로 어떻게 만들어지는지 직접 
커맨드 명령어를 실행하면서 이해해보자.
---------------------------------------------------------
$ cd $GOPATH/src/fabric-samples/
$ cp -r ./first-network ./first-network-copy
$ cd first-network
$ ./byfn.sh generate


---------------------------------------------------------
네트워크 구성
---------------------------------------------------------
$ cd $GOPATH/src/fabric-samples/
$ cp -r ./first-network ./first-network-copy

1. cryptogen 도구를 사용해 인증서를 생성한다.

$ cd $GOPATH/src/fabric-samples/first-network-copy
$ vi crypto-config.yaml		//구성 파악
$ ../bin/cryptogen generate --config=./crypto-config.yaml
$ ls crypto-config

2. configtxgen 도구를 사용해 오더링 서비스 노드의 제네시스 블록을 생성한다.
$ export FABRIC_CFG_PATH=$PWD
$ ../bin/configtxgen -profile TwoOrgsOrdererGenesis -outputBlock ./channel-artifacts/genesis.block

3. configtxgen 도구를 사용해 채널을 생성한다.
$ export CHANNEL_NAME=mychannel
$ ../bin/configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/channel.tx -channelID $CHANNEL_NAME

4. configtxgen 도구를 사용해 Org1MSP에 대한 앵커 피어 노드 트랜잭션 파일을 생성한다.
$ ../bin/configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org1MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org1MSP

5. configtxgen 도구를 사용해 Org2MSP에 대한 앵커 피어 노드 트랜잭션 파일을 생성한다.
$ ../bin/configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org2MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org2MSP

---------------------------------------------------------
네트워크 실행

쉘 스크립트로 up 옵션을 입력했을 때 실행되는 커맨드 명령어를 단계 별로 직접 
입력해 보고 이때 하이퍼레저 패브릭 BYFN 네트워크에서 어떤 일이 일어나는지 분석해본다.
---------------------------------------------------------
$ cd $GOPATH/src/fabric-samples/first-network
$ ./byfn.sh up
$ docker ps
$ ./byfn.sh down

단계별로 네트워크를 시작했을 때 구성되는 BYFN의 프로세스는 다음과 같다 

1. 5개의 노드(오더링 서비스 노드, 4개의 피어 노드) 컨테이너, CLI 컨테이너가 실행되며 총 6개의 컨테이너가 먼저 실행된다.
$ cd $GOPATH/src/fabric-samples/first-network-copy
$ docker-compose -f docker-compose-cli.yaml up -d
$ docker ps 
2. 네트워크 내부의 CLI 컨테이너에 접속해 생성된 채널 트랜잭션 파일인 channel.tx를 가지고 채널을 생성하고 채널 mychannel을 생성하고 모든 피어 노드(2개 조직, 4개 피어 노드)를 가입시킨다.
$ docker start cli
$ docker exec -it cli bash
# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051
# CORE_PEER_LOCALMSPID="Org1MSP"
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
# export CHANNEL_NAME=mychannel
# peer channel create -o orderer.example.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/channel.tx --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
# ls
# peer channel join -b mychannel.block

#CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp 
#CORE_PEER_ADDRESS=peer1.org1.example.com:8051 
#CORE_PEER_LOCALMSPID="Org1MSP" 
#CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls/ca.crt 
#peer channel join -b mychannel.block

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp 
# CORE_PEER_ADDRESS=peer0.org2.example.com:9051 
# CORE_PEER_LOCALMSPID="Org2MSP" 
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt 
# peer channel join -b mychannel.block

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp 
# CORE_PEER_ADDRESS=peer1.org2.example.com:10051 
# CORE_PEER_LOCALMSPID="Org2MSP" 
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls/ca.crt 
# peer channel join -b mychannel.block

3. 두 조직의 peer0를 앵커로 가입시킨다.
# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051 \
# CORE_PEER_LOCALMSPID="Org1MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
# peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/Org1MSPanchors.tx --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org2.example.com:9051 \
# CORE_PEER_LOCALMSPID="Org2MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt \
# peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/Org2MSPanchors.tx --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

4. 피어 노드에 체인코드를 설치하고 확인하는 단계다. 이 단계에서 해당 피어 노드의 체인코드를 담당하는 체인코드 컨테이너 3개가 실행된다.
  - 두 조직의 앵커 피어 노드(peer0.org1, peer0.org2)에 체인코드를 설치한다.
  - peer0.org2에 초깃값 a에 100을, b에 200을 설정한다.
  - peer0.org1에서 a의 값을 조회한다.
  - 트랜잭션 호출(invoke)로 a에서 b로 10만큼 이동한다.
  - 체인코드를 갖고 있지 않은 peer1.org2에 체인코드를 설치한다.
  - peer1.org2에서 값 a를 조회한다. 초깃값인 100에서 전송한 10을 뺀 90이 조회된다.
  
# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051 \
# CORE_PEER_LOCALMSPID="Org1MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
# peer chaincode install -n mycc -v 1.0 -p github.com/chaincode/chaincode_example02/go/

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org2.example.com:9051 \
# CORE_PEER_LOCALMSPID="Org2MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt \
# peer chaincode install -n mycc -v 1.0 -p github.com/chaincode/chaincode_example02/go/

# peer chaincode instantiate -o orderer.example.com:7050 --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C $CHANNEL_NAME -n mycc1 -v 1.0 -c '{"Args":["init","a", "100", "b","200"]}' -P "OR('Org1MSP.peer','Org2MSP.peer')"

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051 \
# CORE_PEER_LOCALMSPID="Org1MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
# peer chaincode query -C $CHANNEL_NAME -n mycc -c '{"Args":["query","a"]}'

# peer chaincode invoke -o orderer.example.com:7050 --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C $CHANNEL_NAME -n mycc -c '{"Args":["invoke","a","b","10"]}'

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp 
# CORE_PEER_ADDRESS=peer1.org2.example.com:10051 
# CORE_PEER_LOCALMSPID="Org2MSP" 
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls/ca.crt 
# peer chaincode install -n mycc -v 1.0 -p github.com/chaincode/chaincode_example02/go/

# peer chaincode query -C $CHANNEL_NAME -n mycc -c '{"Args":["query","a"]}'

# exit
$ docker-compose -f docker-compose-cli.yaml down
$ rm -rf channel-artifacts/*.block channel-artifacts/*.tx crypto-config
$ docker rm $(docker ps -aq) -f


---------------------------------------------------------
체인코드 작성 - Java 

체인코드 작성 후 빌드시 문제발생 
-> 4.4.1 gradle 버전에 맞게 build.gradle 작성 및 wrapper안에 properties파일에 4.4.1로 변경
---------------------------------------------------------
$ docker exec -it cli bash

# peer chaincode install -n mycc3 -v 1.0 -l java -p /opt/gopath/src/github.com/chaincode/basicchaincode/java/
# peer chaincode instantiate -o orderer.example.com:7050 -v 1.0 --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n mycc3 -l java -c '{"Args":["a","10"]}'

# peer chaincode query -C mychannel -n mycc3 -c '{"Args":["get","a"]}'

# peer chaincode invoke --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n mycc3 -c '{"Args":["set","b", "100"]}'

# peer chaincode query -C mychannel -n mycc3 -c '{"Args":["get","b"]}'

# peer chaincode invoke --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n mycc3 -c '{"Args":["transfer","a", "b", "10"]}'


---------------------------------------------------------
지금까지 기본적인 하이퍼레저 패브릭 네트워크를 구축하고 체인코드를 개발해 스마트 컨트랙트가 실행되는 것을 확인하고 학습했다.

---------------------------------------------------------
cd /home/hslee/go/src/fabric-samples/first-network
cd /home/hslee/go/src/careerpath/basic-network


---------------------------------------------------------
하이퍼레저 패브릭 네트워크 구축 
---------------------------------------------------------
1. 서비스 시나리오 

2. 하이퍼레저 패브릭 네트워크 구축 
$ cd $GOPATH/src
$ mkdir careerpath && cd careerpath
$ mkdir basic-network && cd basic-network
$ mkdir crypto-config
$ touch crypto-config.yaml

$ touch configtx.yaml
$ touch docker-compose.yaml
$ export FABRIC_CFG_PATH=$PWD
$ cp -r $GOPATH/src/fabric-samples/bin ./

2-1. 인증서 구성 파일 작성 
$ cd $GOPATH/src/careerpath/basic-network
$ vi crypto-config.yaml
$ ./bin/cryptogen generate --config=./crypto-config.yaml

2-2. 제네시스 블록 및 트랜잭션 생성 
$ cd $GOPATH/src/careerpath/basic-network
$ vi configtx.yaml
$ mkdir config
$ ./bin/configtxgen -profile OrdererGenesis -outputBlock ./config/genesis.block

$ ./bin/configtxgen -profile Channel1 -outputCreateChannelTx ./config/channel1.tx -channelID channelclient1
$ ./bin/configtxgen -profile Channel1 -outputAnchorPeersUpdate ./config/Client1Organchors.tx -channelID channelclient1 -asOrg Client1Org
$ ./bin/configtxgen -profile Channel1 -outputAnchorPeersUpdate ./config/CompanyOrganchors.tx -channelID channelclient1 -asOrg CompanyOrg

2-3. 도커 컨테이너 구성 파일 작성 
$ cd $GOPATH/src/careerpath/basic-network
$ vi docker-compose.yaml  sudo

$ cd $GOPATH/src/careerpath/basic-network
$ touch .env && vi .env
COMPOSE_PROJECT_NAME=net 입력 후 저장

$ docker-compose -f docker-compose.yaml down
$ docker-compose -f docker-compose.yaml up -d orderer.acornpub.com peer0.client1.acornpub.com peer1.client1.acornpub.com peer0.company.acornpub.com peer1.company.acornpub.com cli	


$ docker exec cli peer channel create -o orderer.acornpub.com:7050 -c channelclient1 -f /etc/hyperledger/configtx/channel1.tx

$ docker exec cli peer channel join -b channelclient1.block
$ docker exec cli peer channel update -o orderer.acornpub.com:7050 -c channelclient1 -f /etc/hyperledger/configtx/Client1Organchors.tx

$ docker exec -e "CORE_PEER_ADDRESS=peer1.client1.acornpub.com:7051" cli peer channel join -b channelclient1.block

$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer0.company.acornpub.com:7051" cli peer channel join -b channelclient1.block
$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer0.company.acornpub.com:7051" cli peer channel update -o orderer.acornpub.com:7050 -c channelclient1 -f /etc/hyperledger/configtx/CompanyOrganchors.tx

$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer1.company.acornpub.com:7051" cli peer channel join -b channelclient1.block

3. 체인코드 작성 

$ cd $GOPATH/src/careerpath
$ sudo chown $USER:$USER chaincode
$ cd chaincode
$ mkdir java

프로젝트 java로 이동시키기
에러 이유 build.gradle 
shadowJar {} 작성 내용중 attributes 'Main-Class' 이 부분 변경을 안해서 계속 container exit code1 에러 발생한것임

3-1. 체인코드 테스트 

[터미널1] client1 조직의 peer0 노드 CLI 실행 
$ cd $GOPATH/src/careerpath/basic-network
$ docker stop cli && docker rm cli
$ docker-compose up -d cli 
$ docker exec -it cli bash

# peer chaincode install -l java -n career-cc -v 1.0 -p /opt/gopath/src/chaincode/java
# peer chaincode instantiate -o orderer.acornpub.com:7050 -C channelclient1 -n career-cc -v 1.0 -c '{"Args":[""]}' -P "OR ('Client1Org.member')"
# peer chaincode invoke -o orderer.acornpub.com:7050 -C channelclient1 -n career-cc -c '{"function": "initMember", "Args":[""]}'
# peer chaincode invoke -o orderer.acornpub.com:7050 -C channelclient1 -n career-cc -c '{"function": "getMember", "Args":["lee1487"]}'
# peer chaincode invoke -o orderer.acornpub.com:7050 -C channelclient1 -n career-cc -c '{"function": "setCareer", "Args":["lee1487", "TFM", "programmer", "20191202", "20220506"]}'
# peer chaincode invoke -o orderer.acornpub.com:7050 -C channelclient1 -n career-cc -c '{"function": "getCareer", "Args":["CR0"]}'

4. 애플리케이션을 위한 CA 서버 구성  

docker-compose-ca.yaml 파일 작성
./crypto-config/peerOrganizations/client1.acornpub.com/ca 안의 내용 확인해서 환경변수 설정 내용 작성 
FABRIC_CA_SERVER_CA_CERTFILE: CA 서버 인증서 파일 경로 
FABRIC_CA_SERVER_CA_KEYFILE: CA 서버의 개인 키가 저장된 경로

$ docker-compose -f docker-compose-ca.yaml up -d ca.client1.acornpub.com

5. Node.js SDK 개발 
$ cd $GOPATH/src/careerpath
$ mkdir -p application/sdk && cd application/sdk
$ touch enrollAdmin.js
$ touch registUsers.js
$ cd $GOPATH/src/careerpath/application
$ touch package.json
$ vi package.json
	package.json 내용 입력
$ npm install --save
$ cd $GOPATH/src/careerpath/application
$ touch connection.json && vi connection.json
connection.json에서 디렉토리 내용 찾아서 수정 /home/hslee/go/src/careerpath/basic-network/crypto-config/peerOrganizations/client1.acornpub.com/users/Admin@client1.acornpub.com/msp/keystore 내용 수정
$ cd $GOPATH/src/careerpath/application/sdk
$ vi enrollAdmin.js
$ node enrollAdmin.js
$ vi registUsers.js
$ node registUsers.js
$ docker-compose -f docker-compose.yaml down

chaincode build 에러 발생해서 build.gradle 부분 일부 수정

6. Java SDK 개발 
$ cd $GOPATH/src/careerpath
$ mkdir -p application/sdk && cd application/sdk

sdk 폴더에 bootjar 파일 생성 후 java -jar xxxx.jar 실행
이때 connection.json은 /application/ 위치에 복붙

여기까지가 하이퍼레저 기본 설정 내용들 이였다. 
```

# 하이퍼레저 패브릭 프로젝트 심화 
```
앞에서 설치했던 도커 컨테이너를 전부 내리고 네트워크와 체인코드를 확장하기 위해 다음 커맨드를 실행 
$ cd $GOPATH/src
$ docker stop $(docker ps -a -q)
$ docker rm $(docker ps -a -q)
$ docker rmi -f $(docker images dev-* -q)
$ mkdir careerpath2 && cd careerpath2
$ mkdir basic-network && cd basic-network
$ mkdir crypto-config
$ touch crypto-config.yaml

$ touch configtx.yaml
$ touch docker-compose.yaml
$ export FABRIC_CFG_PATH=$PWD
$ cp -r $GOPATH/src/fabric-samples/bin ./


crypto-config.yaml, configtx.yaml, docker-compose.yaml
$ cd $GOPATH/src/careerpath2/basic-network
$ vi crypto-config.yaml -> Client2 조직 내용 추가 
$ vi configtx.yaml -> 프로필 컨소시엄에서 Client2 조직 내용 추가 Channel2 내용 추가
$ vi docker-compose.yaml -> Client2 조직에 해당하는 peer 노드 2개 추가, cli -> cli1,2 변경

couchDB 기반 피어 노드 구성 
$ vi docker-compose.yaml -> peer 조직에 couchdb 컨테이너 내용 추가

카프카 기반 오더링 서비스 노드 구성 
$ vi crypto-config.yaml -> orderer 노드 추가 
$ vi configtx.yaml -> order타입 변경, 브로커 설정 변경 
$ vi docker-compose.yaml -> orderer1로변경 및 orderer2 추가
$ vi docker-compose.yaml -> 주키퍼 3개에 대한 컨테이너 설정 추가
$ vi docker-compose.yaml -> kafka 4개에 대한 구성 추가 
$ vi docker-compose.yaml -> 모든 피어 노드의 depends_on 설정을 두개의 오더링 서비스 노드 구성으로 변경

base 디렉토리 및 kafka-base.yaml 파일 생성 
$ cd $GOPATH/src/careerpath2/basic-network/
$ mkdir base && cd base
$ touch kafka-base.yaml
$ vi kafka-base.yaml

$ ./bin/cryptogen generate --config=./crypto-config.yaml
$ mkdir config
$ ./bin/configtxgen -profile OrdererGenesis -outputBlock ./config/genesis.block

$ ./bin/configtxgen -profile Channel1 -outputCreateChannelTx ./config/channel1.tx -channelID channelclient1
$ ./bin/configtxgen -profile Channel2 -outputCreateChannelTx ./config/channel2.tx -channelID channelclient2
$ ./bin/configtxgen -profile Channel1 -outputAnchorPeersUpdate ./config/Client1Organchors.tx -channelID channelclient1 -asOrg Client1Org
$ ./bin/configtxgen -profile Channel2 -outputAnchorPeersUpdate ./config/Client2Organchors.tx -channelID channelclient2 -asOrg Client2Org
$ ./bin/configtxgen -profile Channel1 -outputAnchorPeersUpdate ./config/CompanyOrganchorsChannel1.tx -channelID channelclient1 -asOrg CompanyOrg
$ ./bin/configtxgen -profile Channel2 -outputAnchorPeersUpdate ./config/CompanyOrganchorsChannel2.tx -channelID channelclient2 -asOrg CompanyOrg


$ touch .env && vi .env
COMPOSE_PROJECT_NAME=net 입력 후 저장
$ docker-compose -f docker-compose.yaml up -d


$ docker exec cli1 peer channel create -o orderer1.acornpub.com:7050 -c channelclient1 -f /etc/hyperledger/configtx/channel1.tx
$ docker exec cli1 peer channel join -b channelclient1.block
$ docker exec cli1 peer channel update -o orderer1.acornpub.com:7050 -c channelclient1 -f /etc/hyperledger/configtx/Client1Organchors.tx

$ docker exec -e "CORE_PEER_ADDRESS=peer1.client1.acornpub.com:7051" cli1 peer channel join -b channelclient1.block
$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer0.company.acornpub.com:7051" cli1 peer channel join -b channelclient1.block
$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer0.company.acornpub.com:7051" cli1 peer channel update -o orderer1.acornpub.com:7050 -c channelclient1 -f /etc/hyperledger/configtx/CompanyOrganchorsChannel1.tx

$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer1.company.acornpub.com:7051" cli1 peer channel join -b channelclient1.block

$ docker exec cli2 peer channel create -o orderer1.acornpub.com:7050 -c channelclient2 -f /etc/hyperledger/configtx/channel2.tx

$ docker exec cli2 peer channel join -b channelclient2.block
$ docker exec cli2 peer channel update -o orderer1.acornpub.com:7050 -c channelclient2 -f /etc/hyperledger/configtx/Client2Organchors.tx

$ docker exec -e "CORE_PEER_ADDRESS=peer1.client2.acornpub.com:7051" cli2 peer channel join -b channelclient2.block

$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer0.company.acornpub.com:7051" cli2 peer channel join -b channelclient2.block
$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer0.company.acornpub.com:7051" cli2 peer channel update -o orderer1.acornpub.com:7050 -c channelclient2 -f /etc/hyperledger/configtx/CompanyOrganchorsChannel2.tx

$ docker exec -e "CORE_PEER_LOCALMSPID=CompanyOrg" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/company.acornpub.com/users/Admin@company.acornpub.com/msp" -e "CORE_PEER_ADDRESS=peer1.company.acornpub.com:7051" cli2 peer channel join -b channelclient2.block


docker-compose up 후 create channel할때 에러 났었는데 원인은 kafka-base.yaml 오타로 생각된다. 수정내용은 이부분 오타와 zookeeper와 kafka 버전을 0.4.22로 맞췄다는 것 
```

# 하이퍼레저 패브릭 최신버전 설치 
```
-----------------------------------------------------------
최신 하이퍼레저 패브릭 네트워크 구축시 필요 구축 환경설정
-----------------------------------------------------------

1. curl, git 설치 
$ sudo apt install -y curl
$ curl -V   7.58.0

$ sudo apt install -y git 
$ git --version		2.17.1

2. docker 설치
$  sudo apt-get update

$ sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
$ echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
$ sudo apt-get update
$ sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin
$ sudo usermod -aG docker $USER
$ sudo reboot
$ docker -v			20.10.15


3. docker-compose 설치
$ sudo curl -L "https://github.com/docker/compose/releases/download/v2.4.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
$ sudo chmod +x /usr/local/bin/docker-compose
$ docker-compose --version      v2.4.0

3. go 설치
$ cd /usr/local
$ sudo wget https://go.dev/dl/go1.18.2.linux-amd64.tar.gz
$ sudo tar -C /usr/local -xzf go1.18.2.linux-amd64.tar.gz
$ echo 'export PATH=$PATH:/usr/local/go/bin' | sudo tee -a /etc/profile && \
echo 'export GOPATH=$HOME/go' | tee -a $HOME/.bashrc && \
echo 'export PATH=$PATH:$GOROOT/bin:$GOPATH/bin' | tee -a $HOME/.bashrc && \
mkdir -p $HOME/go/{src,pkg,bin}
$ sudo reboot
$ go version	1.18.2

4. node, npm 설치
$ curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash
$ sudo reboot
$ nvm install --lts 		--> node v16.15.0, npm v8.5.5 설치됨

5. java 설치
$ sudo add-apt-repository ppa:openjdk-r/ppa
$ sudo apt update
$ sudo apt install openjdk-11-jdk 
$ javac -version
$ java -version		11.0.15

6. gradle 설치 

$ VERSION=7.4.1
$ wget https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip -P /tmp
$ sudo unzip -d /opt/gradle /tmp/gradle-${VERSION}-bin.zip
$ sudo ln -s /opt/gradle/gradle-${VERSION} /opt/gradle/latest

$ sudo gedit /etc/profile.d/gradle.sh
아래내용 입력후 저장
export GRADLE_HOME=/opt/gradle/latest
export PATH=${GRADLE_HOME}/bin:${PATH}

$ sudo chmod +x /etc/profile.d/gradle.sh
$ source /etc/profile.d/gradle.sh


7. 하이퍼레저 패브릭 설치 
$ cd $GOPATH/src
$ curl -sSL https://bit.ly/2ysbOFE | bash -s -- 2.2.2 1.4.9


-----------------------------------------------------------
하이퍼레저 패브릭 시작하기
fabric-samples/test-network/
-----------------------------------------------------------

$ cd $GOPATH/src/fabric-samples/test-network/
$ ./network.sh up
$ ./network.sh createChannel
$ ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-go -ccl go
$ export PATH=${PWD}/../bin:$PATH
$ export FABRIC_CFG_PATH=$PWD/../config/

$ export CORE_PEER_TLS_ENABLED=true
$ export CORE_PEER_LOCALMSPID="Org1MSP"
$ export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
$ export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
$ export CORE_PEER_ADDRESS=localhost:7051

$ peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile ${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n basic \
--peerAddresses localhost:7051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"function":"InitLedger","Args":[]}'

$ peer chaincode query -C mychannel -n basic -c '{"Args":["GetAllAssets"]}'

$ peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile ${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n basic \
--peerAddresses localhost:7051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"function":"TransferAsset","Args":["asset6","Christopher"]}'

$ export CORE_PEER_TLS_ENABLED=true
$ export CORE_PEER_LOCALMSPID="Org2MSP"
$ export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
$ export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
$ export CORE_PEER_ADDRESS=localhost:9051

$ peer chaincode query -C mychannel -n basic -c '{"Args":["ReadAsset","asset6"]}'
$ ./network.sh down

$ ./network.sh up -ca

-----------------------------------------------------------
체인코드 분석 
-----------------------------------------------------------		
https://github.com/hyperledger/fabric-samples
하이퍼레저 공식 github에서 체인코드 하나씩 분석하면 된다.

asset-transfer-basic/chaincode-java 
체인코드 테스트 
$ ./network.sh up createChannel -c mychannel
$ ./network.sh deployCC -c mychannel -ccn latest-basic-chaincode -ccp ../asset-transfer-basic/chaincode-java/ -ccl java

$ peer chaincode query -C mychannel -n basicj -c '{"Args":["GetAllAssets"]}'
```