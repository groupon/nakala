/*
Copyright (c) 2013, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.groupon.nakala.sentiment;

import com.groupon.nakala.core.Id;
import com.groupon.nakala.core.Review;
import com.groupon.nakala.core.TitledContentArray;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * @author npendar@groupon.com
 */
public class ReviewExtractorTest {

    private Review excerpt(long did, String title, String body) {
        return new Review(new Id(did), title, body, 1.0);
    }

    @Test
    public void testExtract() throws Exception {
        ReviewExtractor re = null;
        PrePostProcessor pp = null;

        re = ReviewExtractor.newInstance(null);
        pp = PrePostProcessor.newInstance();

        ExcerptIndexer ei = new ExcerptIndexer(new WhitespaceAnalyzer(), pp);

        TitledContentArray ses = new TitledContentArray();
        ses.add(excerpt(2, "clean rooms",
                "We really enjoyed our stay. The staff were polite and the rooms were very clean."));
        ses.add(excerpt(4, "aweful hotel",
                "The hotel was aweful. The rooms were filthy and the staff were rude."));
        ses.add(excerpt(6, "rooms smelled",
                "The rooms smelled and were not very clean."));
        ses.add(excerpt(8, "ok day spa",
                "The day spa was ok."));
        ses.add(excerpt(10, "",
                "We loved it! They have the nicest lunch buffet."));
        ses.add(excerpt(12, "",                        // This one shouldn't yield any reviews.
                "My husband and I left the kids with my mother."));
        ses.add(excerpt(14, "100% Satisfactory",
                "A great place for family get-aways."));
        ses.add(excerpt(16, "STAY AWAY",
                "Felt really sorry for my kids."));
        ses.add(excerpt(18, "Great experience",
                "not bad for a family in a hurry."));
        ses.add(excerpt(20, "A bad place to stay",
                "I requested a quiet room and shortly after the front desk put a large family including screaming kids next door (with adjoining door)."));
        ses.add(excerpt(22, "the place to stay in delaware",
                "would definitly return for businesss or pleasure with or without family."));
        ses.add(excerpt(24, "dor127",
                "Its quiet because there is no casino at the location."));
        ses.add(excerpt(26, "A great room and great service",
                "The hotel is in a quiet area, not a bit of noise, and has a friendly staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels."));
        ses.add(excerpt(28, "A great room and great service",
                "Neat, clean and very reasonably priced."));
        ses.add(excerpt(30, "LOOK ELSEWHERE",
                "IF THEY LOWERED THEIR PRICE I WOULD STAY AGAIN BUT NOT AT WHAT THEY CHARGE THESE DAYS."));
        ses.add(excerpt(32, "Mixed Review",
                "In addition, the price has gone up substantially and I don't think the value is as good as it used to be."));
        ses.add(excerpt(34, "Great Amenities But a Little Over Priced",
                "In all, I'd say it was a relatively pleasant stay but I don't think it was worth the price."));
        ses.add(excerpt(36, "Not worth it!!",
                "Our stay at this hotel was not what we wanted I don't know about all the other Econo lodges but this one was not worth the time trip or money"));
        ses.add(excerpt(38, "GROSS!!!!",
                "The bathroom was gross beyond belief and fortunately when I travel, I bring a small supply of my own cleaning items, so I had to scrub and disinfect the entire bathroom from top to bottom."));
        ses.add(excerpt(40, "Still wasn't satisfied",
                "This is the second Quality Inn hotel that I stayed in and I still wasn't completely satisfied."));
        ses.add(excerpt(42, "We visit 3-4 times a year.",
                "We have stayed at the resort in every season including several holidays, enjoyed ourselves every time, and never had a bad experience."));
        ses.add(excerpt(44, "Great place to stay!!",
                "A great hotel, there is a person on the grounds even late night in case of problems."));
        ses.add(excerpt(46, "Keept it a secret in paradise",
                "All in all, I would go back there and hope not a lot of people read this; I fear if people do hear about this little hotel, it will be bombarded by lots of tourists and then the service will go down and then I'll have to find another hotel in paradise."));
        ses.add(excerpt(48, "Only the view is good...",
                "All in all, I would say it was not a very good experience because of the money we paid!"));
        ses.add(excerpt(50, "A Cannon Beach find...",
                "Always clean, very laid back but professional...an all around perfect find in a town that is forgetting it's local owner roots."));
        ses.add(excerpt(52, "Nice place, I'm going back",
                "For anyone looking to be pampered...this is no luxury accomodations, it is what it is...a nice, clean place to stay, convenient to the beautiful Niagra Falls (American &amp; Canadian) and attractions."));
        ses.add(excerpt(54, "Never enough time",
                "Had a great time but too short of a stay."));
        ses.add(excerpt(56, "I love this place!",
                "I have stayed in other hotels in Twin Falls, but my daughters and I decided long ago, that we wouldn't want to stay anywhere else."));
        ses.add(excerpt(58, "STAY AWAY",
                "IT IS ABSENT ANY CHARM"));
        ses.add(excerpt(60, "-2",
                "Mold in bathroom added to the lack of charm of this Morey-abismal motel."));
        ses.add(excerpt(62, "AWESOME!!!!!!!!!!!!",
                "AND not to mention the romantic view from the master bedroom at night."));
        ses.add(excerpt(64, "Great Place",
                "This beautiful romantic bed &amp; breakfast is not only clean, but the breafast is the best we've ever had."));
        ses.add(excerpt(66, "Perfect for families.",
                "If you are single and /or childless be warned, there are kids everywhere!"));
        ses.add(excerpt(68, "Pleasantly Surprised",
                "We noticed families and couples staying there."));
        ses.add(excerpt(70, "Returning next year!!",
                "Kid friendly resort, although we did not take our kids this time."));
        ses.add(excerpt(72, "Wonderful for families!!",
                "I have been going to this place since I was a child and now I take my children there."));
        ses.add(excerpt(74, "Wonderful Place",
                "We would not only highly recommend it for families and couples alike, but we plan to stay there again very soon."));
        ses.add(excerpt(74, "WOW!!!!!",
                "I figure he saved us two trips to outside water parks and saved our family $200.00!!! Top notch entertainment at a top notch resort!!!"));
        ses.add(excerpt(75, "decaying money pit",
                "Well, I can't argue the location. It really is spectacular and the 'Bell' station staff were top drawer (they must be under different management..). But that is where the good ends. The front desk people were rude to me before I even arrived and when I finally got there, seemed generally confused about what they were doing and what was going on in the hotel. They couldn't find my reservation and they tried to blame it on me...(I must have done something wrong...)When they finally did find me, there were no appologies. They were also rude to people who were trying to contact me by phone during my stay. One of the key things I look for in a hotel when I travel, aside from location, is good service. I'm more than happy to pay high prices but I want good service to match. They gave me the feeling they were doing ME a favor by letting me stay there and being friendly and helpful was not part of their job description. On top of all this, the room was horrible. Yes, I did have an ocean view and this was lovely. Inside the room, however, mold was growing everywhere! On the walls, in the air-conditioning unit, in the carpet(which was also grimey)...yuck! I felt I had to wear flip-flops the whole time! The wall paper was peeling off the walls and the water pressure was barely operational. The sheets were clean(I'm not so sure about the bed spread) and I did have clean towels every day, thank God for that. The inside hallways had what looked like old stained wall-to-wall carpets and they all smelled VERY moldy and stale. I realize Key West has a heavily damp climate but this was really bad stuff ladies and gentlemen! On top of all this, the food wasn't really that special. It had the taste of pre-packaged mid-range hotel food. I was very disappointed with just about my whole experience at the Wyndham and I have to blame the upper management. Better management would fix most of the above problems from cleaner, healthier rooms to a more satisfied, happier, service oriented front desk. This lovely hotel would then be transformed from a decaying money pit into the beautiful, graceful, historic landmark it deserves to be."));
        ses.add(excerpt(77, "Don't waste your money",
                "It really put a damper on our romantic vacation in Napa."));
        ses.add(excerpt(79, "AWESOME!!!!!!!!!!!!!!!!!",
                "AND not to mention the romantic view from the master bedroom at night."));
        ses.add(excerpt(81, "DO NOT STAY AT THE HUDSON",
                "DO NOT STAY AT THE HUDSON."));
        ses.add(excerpt(83, "DO NOT STAY HERE",
                "This hotel is a horrible place!"));
        ses.add(excerpt(85, "DO NOT STAY HERE",
                "This hotel is a horrible shithole!"));
        ses.add(excerpt(87, "",
                "Absolutely one of the very top spas in Chicago, yu'll love the atmosphere and the flawless service you'll get there."));
        ses.add(excerpt(89, "",
                "If you can find a good airplane price, the hotels and activities are no more expensive than anywhere else."));
        ses.add(excerpt(91, "",
                "It's not super fancy but it's clean and well kept."));
        ses.add(excerpt(93, "",
                "The two things we did find very pleasing with this hotel was it's location and the king bed itself (The bed was comfortable)."));
        ses.add(excerpt(95, "",
                "WE LOVED EVERYTHING THAT THE HOTEL HAD TO OFFER."));
        // Don't want this to show up
        ses.add(excerpt(97, "",
                "If you want a taste of the real Hawaii and can live without some standard hotel amenities (and especially if you're on a budget), this is the place for you."));
        ses.add(excerpt(97, "",
                "There is a wonderful selection of table games and slots, not to mention the awesome atmosphere!"));
        ses.add(excerpt(99, "",
                "I recently traveled to Winnipeg, and heard that the most elegant hotel in the city was Fort Garry."));
        ses.add(excerpt(100, "",
                "When in Boulder I will nerver stay anywhere else."));
        ses.add(excerpt(102, "",
                "Very nice property in an excellent location."));
        ses.add(excerpt(104, "",
                "i cannot reccomend this hotel more highly."));
        ses.add(excerpt(106, "",
                "We had a dolphin lagoon view room on the first floor for our honeymoon, and listening to the dolphins in the middle of the night was truly a calming experience."));
        ses.add(excerpt(108, "",
                "It is not a large impersonal motel which is a nice change."));
        ses.add(excerpt(110, "",
                "The rooms are well equipped with broadband access, and the amenities are up to par for a top notch hotel."));
        ses.add(excerpt(112, "",
                "Nothing special here but I wouldn't hesitate to stay here again."));
        ses.add(excerpt(114, "",
                "First off, I was impressed with the quality of the reception, there was always somebody to open the door for us and to greet us, not with the usual hotel like smile, but we felt, with genuine warmth."));
        ses.add(excerpt(116, "",
                "Again no assistance from hotel staff at all, they also told her the police wouldn't come, her onstar called the police for her and they came right away (perhaps hotel staff are told to say this because it looks bad having cruisers parked out front all the time)."));
        ses.add(excerpt(118, "",
                "We've stayed at many hotels in our lives, including three in Elkins, and no hotel has ever been cleaner than this one or had a more helpful, personable staff."));
        ses.add(excerpt(120, "",
                "I've stayed here three different times and have never had a bad experience.:-"));
        ses.add(excerpt(122, "",
                "We do not want to stay No where else."));
        ses.add(excerpt(124, "",
                "The Hyatt on the River Walk is always friendly, curtious, and have never had a complaint about the cleanliness of the facilities."));
        ses.add(excerpt(126, "",
                "While not a luxury hotel, it is a good, clean, safe, friendly place to stay... and we'll stay there again in June 2006 on our way North."));
        ses.add(excerpt(128, "",
                "My room was very comfortable and quiet even though a busy street was closeWriter by."));
        ses.add(excerpt(130, "",
                "The setting surrounding it isn't tranquil or serene-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel."));
        ses.add(excerpt(132, "",
                "However, the water was warm and our children loved it despite the small problems."));
        ses.add(excerpt(134, "",
                "Having gone there for over 13 years, I cannot recommend this restaurant highly enough."));
        ses.add(excerpt(136, "",
                "Myself and my family cannot praise this hotel highly enough."));
        ses.add(excerpt(138, "",
                "we stayed at the Steigenberger city for two nights, we picked it based on a pretty cheap price and good reviews. "
                        + "overall the hotel was decent in pretty much every way. the location of the hotel seemed good on the map, but having "
                        + "never been to the city I was a bit surprised by the neighborhood. while finding our way towards the Zeil in the "
                        + "evening we stumbled across some very shabby streets with odd looking \"bars\" and not too friendly looking people, "
                        + "drunks and other odd characters. we also stumbled across the 'City Hotel' of which you can read some pretty "
                        + "interesting stories about in regards of the neighborhood. the walk to Zeil in the end wasn't more than five minutes "
                        + "but I would rather make it from a different direction. with the location being the worst detail, the hotel itself "
                        + "was ok with decent rooms, nothing fancy but ok for the price. hallways were extremely washed out and musky, rooms "
                        + "were definately cleaner and nicer. staff was friendly and the hotel restaurant was a positive surprise for a late "
                        + "night snack. it was also our only hope since there were absolutely no places to get food nearby. overall not much "
                        + "to complain about the place itself, more about the surroundings which felt a bit uncomfortable especially during "
                        + "weekend nights. if I'd come to the city again, I would definately pick a hotel located somewhere else around the downtown area."));
        ses.add(excerpt(140, "", "So, if you want luxury you should probably choose somewhere else."));
        ses.add(excerpt(142, "", "We soon discovered much to our delight that it didn't mean they skimped on luxury."));
        ses.add(excerpt(144, "", "It is definitely THE place to stay if you travel with your pet."));
        ses.add(excerpt(146, "", "My wife and I found this inn on a website that helps people like us find dog friendly places to stay."));
        ses.add(excerpt(148, "", "It is few &amp; far between, in the city of San Francisco, where you can find a Dog Friendly, with no additional charge for the pooch &amp; Free Parking."));
        ses.add(excerpt(150, "", "The only plus was they allowed small pets."));
        ses.add(excerpt(152, "", "The room was spacious, and clean."));
        ses.add(excerpt(154, "", "The large rooms are completely silent, as they were designed to be."));
        ses.add(excerpt(156, "", "Rooms are small."));
        ses.add(excerpt(158, "", "At the time they did not have a room with 2 queen beds, so we opted for one queen and a sofa queen sleeper."));
        ses.add(excerpt(160, "", "Its not cheap but ultimately if you are worrid about saving 100 bucks this is probabaly not the right place for you."));

        HashSet<String> reviews = new HashSet<String>();
        reviews.add("2	0.75	overall	clean rooms	We really enjoyed our stay.	We really enjoyed our stay.	false	false	0.0");
        reviews.add("2	1.0	staff	clean rooms	The staff were polite and the rooms were very clean.	The staff were <B>polite</B> and the rooms were very clean.	true	false	0.0");
        reviews.add("2	0.75	cleanliness	clean rooms	The staff were polite and the rooms were very clean.	The staff were polite and the rooms were very <B>clean</B>.	true	false	0.0");
        reviews.add("4	0.25	cleanliness	aweful hotel	The rooms were filthy and the staff were rude.	The rooms were filthy and the staff were rude.	false	false	0.0");
        reviews.add("4	0.25	staff	aweful hotel	The rooms were filthy and the staff were rude.	The rooms were filthy and the staff were rude.	false	false	0.0");
        reviews.add("4	0.0	overall	aweful hotel	The hotel was aweful.	The hotel was aweful.	true	false	0.0");
        reviews.add("6	0.0	cleanliness	rooms smelled	The rooms smelled and were not very clean.	The rooms smelled and were not very <B>clean</B>.	true	false	0.0");
        reviews.add("8	0.8333333333333333	spa	ok day spa	The day spa was ok.	The day <B>spa</B> was ok.	true	false	0.0");
        reviews.add("10	0.75	food		They have the nicest lunch buffet.	They have the nicest lunch buffet.	false	false	0.0");
        reviews.add("14	0.75	family	100% Satisfactory	A great place for family get-aways.	A great place for <B>family</B> get-aways.	false	false	0.0");
        reviews.add("16	0.25	family	STAY AWAY	Felt really sorry for my kids.	Felt really sorry for my <B>kids</B>.	false	true	0.0");
        reviews.add("18	0.75	family	Great experience	not bad for a family in a hurry.	not bad for a <B>family</B> in a hurry.	false	false	0.0");
        reviews.add("20	0.25	quiet	A bad place to stay	I requested a quiet room and shortly after the front desk put a large family including screaming kids next door (with adjoining door).	I requested a <B>quiet</B> room and shortly after the front desk put a large family including screaming kids next door (with adjoining door).	false	false	0.0");
        reviews.add("20	0.25	romance	A bad place to stay	I requested a quiet room and shortly after the front desk put a large family including screaming kids next door (with adjoining door).	I requested a quiet room and shortly after the front desk put a large family including screaming kids next door (with adjoining door).	false	false	0.0");
        reviews.add("22	0.75	would_return	the place to stay in delaware	would definitly return for businesss or pleasure with or without family.	would definitly return for businesss or pleasure with or without family.	false	false	0.0");
        reviews.add("24	0.75	quiet	dor127	Its quiet because there is no casino at the location.	Its <B>quiet</B> because there is no casino at the location.	false	false	0.0");
        reviews.add("26	1.0	cleanliness	A great room and great service	The hotel is in a quiet area, not a bit of noise, and has a friendly staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels.	The hotel is in a quiet area, not a bit of noise, and has a friendly staff and great maid service; our room was very <B>clean</B>, and I am a bit of a germ freak in hotels.	true	false	0.0");
        reviews.add("26	1.0	quiet	A great room and great service	The hotel is in a quiet area, not a bit of noise, and has a friendly staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels.	The hotel is in a <B>quiet</B> area, not a bit of noise, and has a friendly staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels.	true	false	0.0");
        reviews.add("26	1.0	staff	A great room and great service	The hotel is in a quiet area, not a bit of noise, and has a friendly staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels.	The hotel is in a quiet area, not a bit of noise, and has a <B>friendly</B> staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels.	true	false	0.0");
        reviews.add("26	1.0	overall	A great room and great service	The hotel is in a quiet area, not a bit of noise, and has a friendly staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels.	The hotel is in a quiet area, not a bit of noise, and has a friendly staff and great maid service; our room was very clean, and I am a bit of a germ freak in hotels.	true	false	0.0");
        reviews.add("28	0.75	value	A great room and great service	Neat, clean and very reasonably priced.	Neat, clean and very reasonably <B>priced</B>.	false	false	0.0");
        reviews.add("28	0.75	cleanliness	A great room and great service	Neat, clean and very reasonably priced.	Neat, <B>clean</B> and very reasonably priced.	false	false	0.0");
        reviews.add("30	0.25	overall	LOOK ELSEWHERE	IF THEY LOWERED THEIR PRICE I WOULD STAY AGAIN BUT NOT AT WHAT THEY CHARGE THESE DAYS.	IF THEY LOWERED THEIR PRICE I WOULD STAY AGAIN BUT NOT AT WHAT THEY CHARGE THESE DAYS.	false	true	0.0");
        reviews.add("30	0.25	value	LOOK ELSEWHERE	IF THEY LOWERED THEIR PRICE I WOULD STAY AGAIN BUT NOT AT WHAT THEY CHARGE THESE DAYS.	IF THEY LOWERED THEIR PRICE I WOULD STAY AGAIN BUT NOT AT WHAT THEY CHARGE THESE DAYS.	false	true	0.0");
        reviews.add("32	0.25	value	Mixed Review	In addition, the price has gone up substantially and I don't think the value is as good as it used to be.	In addition, the <B>price</B> has gone up substantially and I don't think the <B>value</B> is as good as it used to be.	false	false	0.0");
        reviews.add("34	0.25	value	Great Amenities But a Little Over Priced	In all, I'd say it was a relatively pleasant stay but I don't think it was worth the price.	In all, I'd say it was a relatively pleasant stay but I don't think it was worth the <B>price</B>.	false	false	0.0");
        reviews.add("34	0.75	overall	Great Amenities But a Little Over Priced	In all, I'd say it was a relatively pleasant stay but I don't think it was worth the price.	In all, I'd say it was a relatively pleasant stay but I don't think it was worth the price.	false	false	0.0");
        reviews.add("36	0.0	value	Not worth it!!	Our stay at this hotel was not what we wanted I don't know about all the other Econo lodges but this one was not worth the time trip or money	Our stay at this hotel was not what we wanted I don't know about all the other Econo lodges but this one was not worth the time trip or money	true	false	0.0");
        reviews.add("38	0.0	cleanliness	GROSS!!!!	The bathroom was gross beyond belief and fortunately when I travel, I bring a small supply of my own cleaning items, so I had to scrub and disinfect the entire bathroom from top to bottom.	The bathroom was gross beyond belief and fortunately when I travel, I bring a small supply of my own cleaning items, so I had to scrub and disinfect the entire bathroom from top to bottom.	true	false	0.0");
        reviews.add("40	0.125	overall	Still wasn't satisfied	This is the second Quality Inn hotel that I stayed in and I still wasn't completely satisfied.	This is the second Quality Inn hotel that I stayed in and I still wasn't completely satisfied.	true	false	0.0");
        reviews.add("42	0.5833333333333334	overall	We visit 3-4 times a year.	We have stayed at the resort in every season including several holidays, enjoyed ourselves every time, and never had a bad experience.	We have stayed at the resort in every season including several holidays, enjoyed ourselves every time, and never had a bad experience.	true	false	0.0");
        reviews.add("44	0.6666666666666666	overall	Great place to stay!!	A great hotel, there is a person on the grounds even late night in case of problems.	A great hotel, there is a person on the grounds even late night in case of problems.	true	false	0.0");
        reviews.add("46	0.5	overall	Keept it a secret in paradise	All in all, I would go back there and hope not a lot of people read this; I fear if people do hear about this little hotel, it will be bombarded by lots of tourists and then the service will go down and then I'll have to find another hotel in paradise.	All in all, I would go back there and hope not a lot of people read this; I fear if people do hear about this little hotel, it will be bombarded by lots of tourists and then the service will go down and then I'll have to find another hotel in paradise.	true	false	0.6666666666666666");
        reviews.add("46	0.6666666666666666	would_return	Keept it a secret in paradise	All in all, I would go back there and hope not a lot of people read this; I fear if people do hear about this little hotel, it will be bombarded by lots of tourists and then the service will go down and then I'll have to find another hotel in paradise.	All in all, I would go back there and hope not a lot of people read this; I fear if people do hear about this little hotel, it will be bombarded by lots of tourists and then the service will go down and then I'll have to find another hotel in paradise.	true	false	0.0");
        reviews.add("48	0.33333333333333337	overall	Only the view is good...	All in all, I would say it was not a very good experience because of the money we paid!	All in all, I would say it was not a very good experience because of the money we paid!	true	false	0.0");
        reviews.add("48	0.33333333333333337	value	Only the view is good...	All in all, I would say it was not a very good experience because of the money we paid!	All in all, I would say it was not a very good experience because of the money we paid!	true	false	0.0");
        reviews.add("50	0.6666666666666666	overall	A Cannon Beach find...	Always clean, very laid back but professional...an all around perfect find in a town that is forgetting it's local owner roots.	Always clean, very laid back but professional...an all around perfect find in a town that is forgetting it's local owner roots.	true	false	0.0");
        reviews.add("50	0.6666666666666666	cleanliness	A Cannon Beach find...	Always clean, very laid back but professional...an all around perfect find in a town that is forgetting it's local owner roots.	Always <B>clean</B>, very laid back but professional...an all around perfect find in a town that is forgetting it's local owner roots.	true	false	0.0");
        reviews.add("52	0.6666666666666666	cleanliness	Nice place, I'm going back	For anyone looking to be pampered...this is no luxury accomodations, it is what it is...a nice, clean place to stay, convenient to the beautiful Niagra Falls (American &amp; Canadian) and attractions.	For anyone looking to be pampered...this is no luxury accomodations, it is what it is...a nice, <B>clean</B> place to stay, convenient to the beautiful Niagra Falls (American &amp; Canadian) and attractions.	true	false	0.0");
        reviews.add("52	0.33333333333333337	luxury	Nice place, I'm going back	For anyone looking to be pampered...this is no luxury accomodations, it is what it is...a nice, clean place to stay, convenient to the beautiful Niagra Falls (American &amp; Canadian) and attractions.	For anyone looking to be <B>pampered</B>...this is no <B>luxury</B> accomodations, it is what it is...a nice, clean place to stay, <B>convenient</B> to the <B>beautiful</B> Niagra Falls (American &amp; Canadian) and attractions.	true	false	0.0");
        reviews.add("52	0.6666666666666666	overall	Nice place, I'm going back	For anyone looking to be pampered...this is no luxury accomodations, it is what it is...a nice, clean place to stay, convenient to the beautiful Niagra Falls (American &amp; Canadian) and attractions.	For anyone looking to be pampered...this is no luxury accomodations, it is what it is...a nice, clean place to stay, <B>convenient</B> to the <B>beautiful</B> Niagra Falls (American &amp; Canadian) and attractions.	true	false	0.0");
        reviews.add("54	0.6666666666666666	overall	Never enough time	Had a great time but too short of a stay.	Had a great time but too short of a stay.	true	false	0.0");
        reviews.add("56	0.6666666666666666	overall	I love this place!	I have stayed in other hotels in Twin Falls, but my daughters and I decided long ago, that we wouldn't want to stay anywhere else.	I have stayed in other hotels in Twin Falls, but my daughters and I decided long ago, that we wouldn't want to stay anywhere else.	true	false	0.0");
        reviews.add("60	0.25	cleanliness	-2	Mold in bathroom added to the lack of charm of this Morey-abismal motel.	Mold in bathroom added to the lack of charm of this Morey-abismal motel.	false	false	0.0");
        reviews.add("60	0.25	overall	-2	Mold in bathroom added to the lack of charm of this Morey-abismal motel.	Mold in bathroom added to the lack of charm of this Morey-abismal motel.	false	false	0.0");
        reviews.add("62	0.75	overall	AWESOME!!!!!!!!!!!!	AND not to mention the romantic view from the master bedroom at night.	AND not to mention the romantic view from the master bedroom at night.	false	false	0.0");
        reviews.add("64	0.75	romance	Great Place	This beautiful romantic bed &amp; breakfast is not only clean, but the breafast is the best we've ever had.	This <B>beautiful</B> <B>romantic</B> bed &amp; breakfast is not only clean, but the breafast is the <B>best</B> we've ever had.	false	false	0.0");
        reviews.add("64	0.75	cleanliness	Great Place	This beautiful romantic bed &amp; breakfast is not only clean, but the breafast is the best we've ever had.	This beautiful romantic bed &amp; breakfast is not only <B>clean</B>, but the breafast is the best we've ever had.	false	false	0.0");
        reviews.add("64	0.75	overall	Great Place	This beautiful romantic bed &amp; breakfast is not only clean, but the breafast is the best we've ever had.	This <B>beautiful</B> romantic bed &amp; breakfast is not only clean, but the breafast is the <B>best</B> we've ever had.	false	false	0.0");
        reviews.add("64	0.75	food	Great Place	This beautiful romantic bed &amp; breakfast is not only clean, but the breafast is the best we've ever had.	This <B>beautiful</B> romantic bed &amp; breakfast is not only clean, but the breafast is the <B>best</B> we've ever had.	false	false	0.0");
        reviews.add("66	0.25	romance	Perfect for families.	If you are single and /or childless be warned, there are kids everywhere!	If you are single and /or childless be warned, there are kids everywhere!	false	false	0.0");
//		reviews.add("68	0.75	family	Pleasantly Surprised	We noticed families and couples staying there.	We noticed <B>families</B> and couples staying there.	false	false	0.0");
        reviews.add("70	0.5	family	Returning next year!!	Kid friendly resort, although we did not take our kids this time.	Kid friendly resort, although we did not take our <B>kids</B> this time.	false	false	0.75");
        reviews.add("72	0.75	family	Wonderful for families!!	I have been going to this place since I was a child and now I take my children there.	I have been going to this place since I was a <B>child</B> and now I take my <B>children</B> there.	false	false	0.0");
        reviews.add("74	0.75	overall	Wonderful Place	We would not only highly recommend it for families and couples alike, but we plan to stay there again very soon.	We would not only highly recommend it for families and couples alike, but we plan to stay there again very soon.	false	false	0.0");
        reviews.add("74	0.75	family	Wonderful Place	We would not only highly recommend it for families and couples alike, but we plan to stay there again very soon.	We would not only highly recommend it for <B>families</B> and couples alike, but we plan to stay there again very soon.	false	false	0.0");
        reviews.add("74	0.75	romance	Wonderful Place	We would not only highly recommend it for families and couples alike, but we plan to stay there again very soon.	We would not only highly recommend it for families and <B>couples</B> alike, but we plan to stay there again very soon.	false	false	0.0");
        reviews.add("75	0.25	staff	decaying money pit	The front desk people were rude to me before I even arrived and when I finally got there, seemed generally confused about what they were doing and what was going on in the hotel.	The front desk people were rude to me before I even arrived and when I finally got there, seemed generally confused about what they were doing and what was going on in the hotel.	false	false	0.0");
        reviews.add("75	0.75	staff	decaying money pit	It really is spectacular and the'Bell' station staff were top drawer (they must be under different management..).	It really is spectacular and the'Bell' station staff were top drawer (they must be under different management..).	false	false	0.0");
//		reviews.add("75	0.25	staff	decaying money pit	I was very disappointed with just about my whole experience at the Wyndham and I have to blame the upper management.	I was very disappointed with just about my whole experience at the Wyndham and I have to blame the upper management.	false	false	0.0");
        reviews.add("75	0.25	overall	decaying money pit	I was very disappointed with just about my whole experience at the Wyndham and I have to blame the upper management.	I was very disappointed with just about my whole experience at the Wyndham and I have to blame the upper management.	false	false	0.0");
        reviews.add("75	0.25	value	decaying money pit	I'm more than happy to pay high prices but I want good service to match.	I'm more than happy to pay high <B>prices</B> but I want good service to match.	false	false	0.0");
        reviews.add("75	0.75	cleanliness	decaying money pit	The sheets were clean (I'm not so sure about the bed spread) and I did have clean towels every day, thank God for that.	The sheets were <B>clean</B> (I'm not so sure about the bed spread) and I did have <B>clean</B> towels every day, thank God for that.	false	false	0.0");
        reviews.add("75	0.25	overall	decaying money pit	I realize Key West has a heavily damp climate but this was really bad stuff ladies and gentlemen!	I realize Key West has a heavily damp climate but this was really bad stuff ladies and gentlemen!	false	false	0.0");
        reviews.add("75	0.25	cleanliness	decaying money pit	The inside hallways had what looked like old stained wall-to-wall carpets and they all smelled VERY moldy and stale.	The inside hallways had what looked like old stained wall-to-wall carpets and they all smelled VERY moldy and stale.	false	false	0.0");
        reviews.add("75	0.25	cleanliness	decaying money pit	Inside the room, however, mold was growing everywhere!	Inside the room, however, mold was growing everywhere!	false	false	0.0");
        reviews.add("75	0.0	value	decaying money pit	This lovely hotel would then be transformed from a decaying money pit into the beautiful, graceful, historic landmark it deserves to be.	This lovely hotel would then be transformed from a decaying money pit into the <B>beautiful</B>, graceful, historic landmark it deserves to be.	true	false	0.0");
        // This isn't quite right. Need to understand subjunctive.
        reviews.add("75	0.75	overall	decaying money pit	They gave me the feeling they were doing ME a favor by letting me stay there and being friendly and helpful was not part of their job description.	They gave me the feeling they were doing ME a favor by letting me stay there and being friendly and helpful was not part of their job description.	false	false	0.0");
        // This isn't quite right. Need to understand subjunctive.
        reviews.add("75	0.75	view	decaying money pit	Yes, I did have an ocean view and this was lovely.	Yes, I did have an ocean <B>view</B> and this was lovely.	false	false	0.0");
        reviews.add("75	1.0	overall	decaying money pit	This lovely hotel would then be transformed from a decaying money pit into the beautiful, graceful, historic landmark it deserves to be.	This lovely hotel would then be transformed from a decaying money pit into the <B>beautiful</B>, graceful, historic landmark it deserves to be.	true	false	0.0");
        reviews.add("75	0.25	food	decaying money pit	On top of all this, the food wasn't really that special.	On top of all this, the food wasn't really that special.	false	false	0.0");
        reviews.add("77	0.25	romance	Don't waste your money	It really put a damper on our romantic vacation in Napa.	It really put a damper on our <B>romantic</B> vacation in Napa.	false	false	0.0");
        reviews.add("79	0.75	romance	AWESOME!!!!!!!!!!!!!!!!!	AND not to mention the romantic view from the master bedroom at night.	AND not to mention the <B>romantic</B> view from the master bedroom at night.	false	false	0.0");
        reviews.add("79	0.75	view	AWESOME!!!!!!!!!!!!!!!!!	AND not to mention the romantic view from the master bedroom at night.	AND not to mention the romantic <B>view</B> from the master bedroom at night.	false	false	0.0");
//		reviews.add("81	0.0	overall	DO NOT STAY AT THE HUDSON	DO NOT STAY AT THE HUDSON.	DO NOT STAY AT THE HUDSON.	true	true	0.0");
        reviews.add("83	0.25	overall	DO NOT STAY HERE	This hotel is a horrible place!	This hotel is a horrible place!	false	true	0.0");
        reviews.add("87	0.75	cleanliness		Absolutely one of the very top spas in Chicago, yu'll love the atmosphere and the flawless service you'll get there.	Absolutely one of the very top spas in Chicago, yu'll love the atmosphere and the <B>flawless</B> service you'll get there.	false	false	0.0");
        reviews.add("87	0.75	overall		Absolutely one of the very top spas in Chicago, yu'll love the atmosphere and the flawless service you'll get there.	Absolutely one of the very top spas in Chicago, yu'll love the atmosphere and the <B>flawless</B> service you'll get there.	false	false	0.0");
        reviews.add("89	0.75	value		If you can find a good airplane price, the hotels and activities are no more expensive than anywhere else.	If you can find a good airplane <B>price</B>, the hotels and activities are no more <B>expensive</B> than anywhere else.	false	false	0.0");
        reviews.add("89	0.75	overall		If you can find a good airplane price, the hotels and activities are no more expensive than anywhere else.	If you can find a good airplane price, the hotels and activities are no more expensive than anywhere else.	false	false	0.0");
        reviews.add("91	0.5	cleanliness		It's not super fancy but it's clean and well kept.	It's not super fancy but it's <B>clean</B> and well kept.	false	false	0.25");
        reviews.add("91	0.25	luxury		It's not super fancy but it's clean and well kept.	It's not super <B>fancy</B> but it's clean and well kept.	false	false	0.0");
        reviews.add("93	0.75	roomcomfortable		The two things we did find very pleasing with this hotel was it's location and the king bed itself (The bed was comfortable).	The two things we did find very pleasing with this hotel was it's location and the king bed itself (The bed was <B>comfortable</B>).	false	false	0.0");
        reviews.add("93	0.75	overall		The two things we did find very pleasing with this hotel was it's location and the king bed itself (The bed was comfortable).	The two things we did find very pleasing with this hotel was it's location and the king bed itself (The bed was comfortable).	false	false	0.0");
        reviews.add("95	0.75	overall		WE LOVED EVERYTHING THAT THE HOTEL HAD TO OFFER.	WE LOVED EVERYTHING THAT THE HOTEL HAD TO OFFER.	false	false	0.0");
        reviews.add("97	0.75	overall		There is a wonderful selection of table games and slots, not to mention the awesome atmosphere!	There is a <B>wonderful</B> selection of table games and slots, not to mention the <B>awesome</B> atmosphere!	false	false	0.0");
        reviews.add("97	0.75	value		If you want a taste of the real Hawaii and can live without some standard hotel amenities (and especially if you're on a budget), this is the place for you.	If you want a taste of the real Hawaii and can live without some standard hotel amenities (and especially if you're on a <B>budget</B>), this is the place for you.	false	false	0.0");
        reviews.add("99	0.75	overall		I recently traveled to Winnipeg, and heard that the most elegant hotel in the city was Fort Garry.	I recently traveled to Winnipeg, and heard that the most <B>elegant</B> hotel in the city was Fort Garry.	false	false	0.0");
        reviews.add("100	0.75	overall		When in Boulder I will nerver stay anywhere else.	When in Boulder I will nerver stay anywhere else.	false	false	0.0");
        reviews.add("102	0.75	overall		Very nice property in an excellent location.	Very nice property in an <B>excellent</B> location.	false	false	0.0");
        reviews.add("104	0.75	overall		i cannot reccomend this hotel more highly.	i cannot reccomend this hotel more highly.	false	false	0.0");
        reviews.add("106	0.75	overall		We had a dolphin lagoon view room on the first floor for our honeymoon, and listening to the dolphins in the middle of the night was truly a calming experience.	We had a dolphin lagoon view room on the first floor for our honeymoon, and listening to the dolphins in the middle of the night was truly a <B>calming</B> experience.	false	false	0.0");
        reviews.add("106	0.75	romance		We had a dolphin lagoon view room on the first floor for our honeymoon, and listening to the dolphins in the middle of the night was truly a calming experience.	We had a dolphin lagoon view room on the first floor for our <B>honeymoon</B>, and listening to the dolphins in the middle of the night was truly a <B>calming</B> experience.	false	false	0.0");
        reviews.add("108	0.75	overall		It is not a large impersonal motel which is a nice change.	It is not a large impersonal motel which is a nice change.	false	false	0.0");
        reviews.add("110	0.75	overall		The rooms are well equipped with broadband access, and the amenities are up to par for a top notch hotel.	The rooms are well equipped with broadband access, and the amenities are up to par for a top notch hotel.	false	false	0.0");
        reviews.add("112	0.75	overall		Nothing special here but I wouldn't hesitate to stay here again.	Nothing special here but I wouldn't hesitate to stay here again.	false	false	0.0");
        reviews.add("116	0.25	staff		Again no assistance from hotel staff at all, they also told her the police wouldn't come, her onstar called the police for her and they came right away (perhaps hotel staff are told to say this because it looks bad having cruisers parked out front all the time).	Again no assistance from hotel staff at all, they also told her the police wouldn't come, her onstar called the police for her and they came right away (perhaps hotel staff are told to say this because it looks bad having cruisers parked out front all the time).	false	false	0.0");
        reviews.add("118	0.75	overall		We've stayed at many hotels in our lives, including three in Elkins, and no hotel has ever been cleaner than this one or had a more helpful, personable staff.	We've stayed at many hotels in our lives, including three in Elkins, and no hotel has ever been cleaner than this one or had a more helpful, personable staff.	false	false	0.0");
        reviews.add("118	0.75	cleanliness		We've stayed at many hotels in our lives, including three in Elkins, and no hotel has ever been cleaner than this one or had a more helpful, personable staff.	We've stayed at many hotels in our lives, including three in Elkins, and no hotel has ever been cleaner than this one or had a more helpful, personable staff.	false	false	0.0");
        reviews.add("120	0.75	overall		I've stayed here three different times and have never had a bad experience.:-	I've stayed here three different times and have never had a bad experience.:-	false	false	0.0");
        reviews.add("122	0.75	overall		We do not want to stay No where else.	We do not want to stay No where else.	false	false	0.0");
        reviews.add("124	0.75	overall		The Hyatt on the River Walk is always friendly, curtious, and have never had a complaint about the cleanliness of the facilities.	The Hyatt on the River Walk is always friendly, curtious, and have never had a complaint about the cleanliness of the facilities.	false	false	0.0");
        reviews.add("126	0.75	cleanliness		While not a luxury hotel, it is a good, clean, safe, friendly place to stay...	While not a luxury hotel, it is a good, <B>clean</B>, safe, friendly place to stay...	false	false	0.0");
        reviews.add("126	0.25	luxury		While not a luxury hotel, it is a good, clean, safe, friendly place to stay...	While not a <B>luxury</B> hotel, it is a good, clean, safe, friendly place to stay...	false	false	0.0");
        reviews.add("126	0.75	overall		And we'll stay there again in June 2006 on our way North.	And we'll stay there again in June 2006 on our way North.	false	false	0.0");
        reviews.add("126	0.75	overall		While not a luxury hotel, it is a good, clean, safe, friendly place to stay...	While not a luxury hotel, it is a good, clean, safe, friendly place to stay...	false	false	0.0");
        reviews.add("128	0.75	roomcomfortable		My room was very comfortable and quiet even though a busy street was closeWriter by.	My room was very <B>comfortable</B> and quiet even though a busy street was closeWriter by.	false	false	0.0");
        reviews.add("128	0.75	quiet		My room was very comfortable and quiet even though a busy street was closeWriter by.	My room was very comfortable and <B>quiet</B> even though a busy street was closeWriter by.	false	false	0.0");
        reviews.add("130	0.75	family		The setting surrounding it isn't tranquil or serene-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel.	The setting surrounding it isn't tranquil or serene-just plain busy but if you are looking for a nice place to stay while in the area our <B>family</B> highly recommends this hotel.	false	false	0.0");
        reviews.add("130	0.5833333333333334	overall		The setting surrounding it isn't tranquil or serene-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel.	The setting surrounding it isn't tranquil or serene-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel.	false	false	0.25");
        reviews.add("130	0.25	quiet		The setting surrounding it isn't tranquil or serene-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel.	The setting surrounding it isn't <B>tranquil</B> or <B>serene</B>-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel.	false	false	0.0");
        reviews.add("130	0.25	romance		The setting surrounding it isn't tranquil or serene-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel.	The setting surrounding it isn't <B>tranquil</B> or serene-just plain busy but if you are looking for a nice place to stay while in the area our family highly recommends this hotel.	false	false	0.0");
        reviews.add("132	0.75	family		However, the water was warm and our children loved it despite the small problems.	However, the water was warm and our <B>children</B> loved it despite the small problems.	false	false	0.0");
        reviews.add("134	0.75	food		Having gone there for over 13 years, I cannot recommend this restaurant highly enough.	Having gone there for over 13 years, I cannot recommend this restaurant highly enough.	false	false	0.0");
//		reviews.add("136	0.75	family		Myself and my family cannot praise this hotel highly enough.	Myself and my <B>family</B> cannot praise this hotel highly enough.	false	false	0.0");
        reviews.add("136	0.75	overall		Myself and my family cannot praise this hotel highly enough.	Myself and my family cannot praise this hotel highly enough.	false	false	0.0");
        reviews.add("138	0.25	luxury		With the location being the worst detail, the hotel itself was ok with decent rooms, nothing fancy but ok for the price.	With the location being the worst detail, the hotel itself was ok with decent rooms, nothing <B>fancy</B> but ok for the price.	false	false	0.0");
        reviews.add("138	0.75	value		With the location being the worst detail, the hotel itself was ok with decent rooms, nothing fancy but ok for the price.	With the location being the worst detail, the hotel itself was ok with decent rooms, nothing fancy but ok for the <B>price</B>.	false	false	0.0");
        reviews.add("138	0.25	cleanliness		While finding our way towards the Zeil in the evening we stumbled across some very shabby streets with odd looking bars and not too friendly looking people, drunks and other odd characters.	While finding our way towards the Zeil in the evening we stumbled across some very shabby streets with odd looking bars and not too friendly looking people, drunks and other odd characters.	false	false	0.0");
        reviews.add("138	0.75	overall		With the location being the worst detail, the hotel itself was ok with decent rooms, nothing fancy but ok for the price.	With the location being the worst detail, the hotel itself was ok with decent rooms, nothing fancy but ok for the price.	false	false	0.0");
        reviews.add("138	0.25	food		It was also our only hope since there were absolutely no places to get food nearby.	It was also our only hope since there were absolutely no places to get food nearby.	false	false	0.0");
        reviews.add("138	0.75	food		Staff was friendly and the hotel restaurant was a positive surprise for a late night snack.	Staff was friendly and the hotel restaurant was a positive surprise for a late night snack.	false	false	0.0");
        reviews.add("138	0.75	staff		Staff was friendly and the hotel restaurant was a positive surprise for a late night snack.	Staff was <B>friendly</B> and the hotel restaurant was a positive surprise for a late night snack.	false	false	0.0");
        reviews.add("138	0.75	overall		Overall not much to complain about the place itself, more about the surroundings which felt a bit uncomfortable especially during weekend nights.	Overall not much to complain about the place itself, more about the surroundings which felt a bit uncomfortable especially during weekend nights.	false	false	0.0");
        reviews.add("138	0.75	overall		Overall the hotel was decent in pretty much every way.	Overall the hotel was decent in pretty much every way.	false	false	0.0");
        reviews.add("138	0.75	value		we stayed at the Steigenberger city for two nights, we picked it based on a pretty cheap price and good reviews.	we stayed at the Steigenberger city for two nights, we picked it based on a pretty <B>cheap</B> <B>price</B> and good reviews.	false	false	0.0");
        reviews.add("140	0.25	luxury		So, if you want luxury you should probably choose somewhere else.	So, if you want <B>luxury</B> you should probably choose somewhere else.	false	false	0.0");
        reviews.add("142	0.75	luxury		We soon discovered much to our delight that it didn't mean they skimped on luxury.	We soon discovered much to our <B>delight</B> that it didn't mean they skimped on <B>luxury</B>.	false	false	0.0");
        reviews.add("144	0.75	overall		It is definitely THE place to stay if you travel with your pet.	It is definitely THE place to stay if you travel with your pet.	false	false	0.0");
        reviews.add("146	0.75	pet		My wife and I found this inn on a website that helps people like us find dog friendly places to stay.	My wife and I found this inn on a website that helps people like us find dog friendly places to stay.	false	false	0.0");
        reviews.add("146	0.75	overall		My wife and I found this inn on a website that helps people like us find dog friendly places to stay.	My wife and I found this inn on a website that helps people like us find dog friendly places to stay.	false	false	0.0");
        reviews.add("148	0.75	value		It is few &amp; far between, in the city of San Francisco, where you can find a Dog Friendly, with no additional charge for the pooch &amp; Free Parking.	It is few &amp; far between, in the city of San Francisco, where you can find a Dog Friendly, with no additional charge for the pooch &amp; Free Parking.	false	false	0.0");
        reviews.add("148	0.75	pet		It is few &amp; far between, in the city of San Francisco, where you can find a Dog Friendly, with no additional charge for the pooch &amp; Free Parking.	It is few &amp; far between, in the city of San Francisco, where you can find a Dog Friendly, with no additional charge for the pooch &amp; Free Parking.	false	false	0.0");
        reviews.add("148	0.75	parking		It is few &amp; far between, in the city of San Francisco, where you can find a Dog Friendly, with no additional charge for the pooch &amp; Free Parking.	It is few &amp; far between, in the city of San Francisco, where you can find a Dog Friendly, with no additional charge for the pooch &amp; Free Parking.	false	false	0.0");
        reviews.add("150	0.75	pet		The only plus was they allowed small pets.	The only plus was they allowed small pets.	false	false	0.0");
        reviews.add("152	0.75	cleanliness		The room was spacious, and clean.	The room was spacious, and <B>clean</B>.	false	false	0.0");
        reviews.add("152	0.75	room_size		The room was spacious, and clean.	The room was spacious, and clean.	false	false	0.0");
        reviews.add("154	0.75	quiet		The large rooms are completely silent, as they were designed to be.	The large rooms are completely <B>silent</B>, as they were designed to be.	false	false	0.0");
        reviews.add("154	0.75	room_size		The large rooms are completely silent, as they were designed to be.	The large rooms are completely silent, as they were designed to be.	false	false	0.0");
        reviews.add("156	0.25	room_size		Rooms are small.	Rooms are small.	false	false	0.0");
        reviews.add("160	0.25	value		Its not cheap but ultimately if you are worrid about saving 100 bucks this is probabaly not the right place for you.	Its not <B>cheap</B> but ultimately if you are worrid about saving 100 bucks this is probabaly not the right place for you.	false	false	0.0");

        ei.index(ses);

        ExtractedRecordCollector erc = null;

        long t1 = System.currentTimeMillis();

        erc = re.extract(ei.getIndexSearcher(), pp);

        long t2 = System.currentTimeMillis();

        Set<ExtractedReviewRecord> records =
                erc.getTableToOutput();
        for (ExtractedReviewRecord er : records) {
            String erStr = er.toString();
            System.out.println(erStr);
            System.out.println("  Title Words: " + er.getTitleWords() + "\n");
//			assertTrue(reviews.contains(erStr));
        }
        if (reviews.size() != records.size()) {
            HashSet<String> recordsStr = new HashSet<String>();
            for (ExtractedReviewRecord er : records) {
                recordsStr.add(er.toString());
            }
            for (String r : reviews) {
                if (!recordsStr.contains(r)) {
                    System.out.println("Missing: " + r);
                }
            }
            System.out.println("Unexpected number of quotes extracted. Expected "
                    + reviews.size() + " but got " + records.size());
            fail();
        }

        System.out.println("Walltime: " + (t2 - t1) + " ms");

    }

}
