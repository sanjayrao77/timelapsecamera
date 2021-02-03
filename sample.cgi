#!/usr/bin/perl

# This code is public domain

select STDOUT;
$| = 1;


$direction='camera';
if ($ENV{'QUERY_STRING'} eq "direction=front") {
	$direction='front';
} elsif ($ENV{'QUERY_STRING'} eq "direction=back") {
	$direction='back';
} elsif ($ENV{'QUERY_STRING'} eq "direction=main") {
	$direction='main';
}

if ($ENV{'CONTENT_LENGTH'} > 8*1024*1024) {
	exit;
}

read(STDIN, $buffer, $ENV{'CONTENT_LENGTH'});

open (H,">/tmp/camera-".$direction.".jpg");
print H "$buffer";
close H;

print "Content-type: text/plain\r\n\r\n{\"success\":1}";
