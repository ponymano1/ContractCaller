package com.oracle.auto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.web3j.abi.datatypes.Uint;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@SpringBootApplication
public class AutoApplication {
    //add log

    private static final String NODE_URL = "http://127.0.0.1:8545";
    private static final String PRIVATE_KEY = "";

    private static final String CONTRACT_ADDRESS1 = "0xe78A0F7E598Cc8b0Bb87894B0F60dD2a88d6a8Ab";
    private static final String FUNC_NAME1 = "claimETHYield";
    private static final String CONTRACT_ADDRESS2 = "0x26b4AFb60d6C903165150C6F0AA14F8016bE4aec";
    private static final String FUNC_NAME2 = "claimUSDBYield";

    // Create a Logger
    private static final Logger LOGGER = Logger.getLogger(AutoApplication.class.getName());


    static {
        try {
            // This block configure the logger with handler and formatter
            FileHandler fh = new FileHandler("AutoClaim.log", true); // Set to append
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            LOGGER.addHandler(fh);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * call smartcontract function
     * @param web3j
     * @param credentials
     * @param transactionManager
     * @param contractAddress contract address
     * @param funcName contract's function name
     * @throws IOException
     */
    public static void call(Web3j web3j,
                            Credentials credentials,
                            TransactionManager transactionManager,
                            String contractAddress,
                            String funcName
                            ) throws IOException {
        // Define the function call
        Function function = new Function(
                funcName,  // function name
                Collections.emptyList(), // input parameters
                //Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {})
                Collections.emptyList()
        ); // output parameters
        LOGGER.info("call:" + contractAddress + " " + funcName);
        System.out.println("call:" + contractAddress + " " + funcName);
        // Encode the function call to a hexadecimal string
        String functionHexString = FunctionEncoder.encode(function);

        // Define gas price and gas limit
        BigInteger gasPrice = DefaultGasProvider.GAS_PRICE;
        BigInteger gasLimit = DefaultGasProvider.GAS_LIMIT;

        // Fetch the next available nonce for the account

        BigInteger nonce = web3j.ethGetTransactionCount(
                        credentials.getAddress(), DefaultBlockParameterName.LATEST)
                .send()
                .getTransactionCount();

        // Value to transfer (set to 0 if not sending ether)
        BigInteger value = BigInteger.ZERO;

        // Send the transaction
        EthSendTransaction transactionResponse = transactionManager.sendTransaction(
                gasPrice,
                gasLimit,
                contractAddress,
                functionHexString,
                value
        );

        // Handle the response
        if (transactionResponse.hasError()) {
            LOGGER.info("Transaction Error: " + transactionResponse.getError().getMessage());
            System.out.println("Transaction Error: " + transactionResponse.getError().getMessage());
        } else {
            String transactionHash = transactionResponse.getTransactionHash();
            System.out.println("Transaction successful: " + transactionHash);
            LOGGER.info("Transaction successful: " + transactionHash);
        }

    }


    public static void main(String[] args) {
        SpringApplication.run(AutoApplication.class, args);
        // Initialize Web3j service
        Web3j web3j = Web3j.build(new HttpService(NODE_URL));

        // Load credentials for the Ethereum account
        Credentials credentials = Credentials.create(PRIVATE_KEY);

        // Create a TransactionManager
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials);
        try {
            call(web3j, credentials, transactionManager,CONTRACT_ADDRESS1,FUNC_NAME1);
            call(web3j, credentials, transactionManager,CONTRACT_ADDRESS2,FUNC_NAME2);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
