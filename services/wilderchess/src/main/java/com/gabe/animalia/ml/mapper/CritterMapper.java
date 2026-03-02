// package com.gabe.animalia.ml.mapper;
// import com.gabe.animalia.ml.dtos.FighterStateDTO;
// import com.gabe.animalia.general.Critter;
// /**
//  * Utility class to map the live Fighter Entity to the simplified FighterStateDto.
//  */
// public class CritterMapper {

//     /**
//      * Maps a live Fighter to a loggable DTO.
//      * We use the fields we defined in Fighter.java (name, health, positionKey).
//      */
//     public static FighterStateDTO toFighterStateDTO(Critter fighter) {
//         if (fighter == null) return null;

//         // Extracting only the primitive facts needed for ML.
//         return new FighterStateDTO(
//             fighter.getName() + "-" + System.identityHashCode(fighter), // Unique ID for this instance
//             fighter.getName(),
//             fighter.getHealth(),
//             100, // Placeholder for MaxHP (add to Fighter.java if needed)
//             0,   // Placeholder for Energy (add to Fighter.java if needed)
//             fighter.getSpot().getName(),
//             0,     // Placeholder for Buffs
//             false  // Placeholder for Stun status
//         );
//     }
// }
