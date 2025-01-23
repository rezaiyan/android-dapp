//SPDX-License-Identifier: Unlicense

pragma solidity ^0.8.0;

contract KickOff {

    struct Candidate {
        string name;
        uint256 votes;
    }

    mapping(address => bool) hasVoted;
    Candidate[] public candidates;

        constructor() {
            candidates.push(Candidate("Bukayo Saka (Arsenal)", 0));
            candidates.push(Candidate("Mohamed Salah (Liverpool)", 0));
            candidates.push(Candidate("Kevin De Bruyne (Manchester City)", 0));
            candidates.push(Candidate("Lionel Messi (Inter Miami)", 0));
            candidates.push(Candidate("Rodri (Man City)", 0));
            candidates.push(Candidate("Jude Bellingham (Real Madrid)", 0));
            candidates.push(Candidate("Vinicius Jr (Real Madrid)", 0));
            candidates.push(Candidate("Erling Haaland (Man City)", 0));
            candidates.push(Candidate("Harry Kane (Bayern Munich)", 0));
            candidates.push(Candidate("Kylian Mbappe (Real Madrid)", 0));
    }

    function getCandidates() public view returns (Candidate[] memory) {
        return candidates;
    }

   function vote(uint256 candidateIndex) public {
        require(!hasVoted[msg.sender], "You have voted already!");
        require(candidateIndex < candidates.length, "The candidate is not valid!");

        hasVoted[msg.sender] = true;
        candidates[candidateIndex].votes += 1;
   }



}